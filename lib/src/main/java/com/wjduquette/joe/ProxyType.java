package com.wjduquette.joe;

import com.wjduquette.joe.types.TypeType;

import java.util.*;
import java.util.function.Function;

/**
 * A JoeType that serves as a proxy for a native Java type.  The proxy type
 * implements all required metadata and services for the native type.
 * @param <V> The native value type
 */
public class ProxyType<V>
    implements JoeClass, JoeValue, NativeCallable
{
    //-------------------------------------------------------------------------
    // Instance Variables

    // Type V's script-level name.  May differ from the Java-level
    // name.
    private final String name;

    // Static methods and constants
    private boolean isStatic = false;
    private ProxyType<? super V> superProxy = null;
    private final Map<String, NativeFunction> staticMethods =
        new HashMap<>();
    private final Map<String, Object> constants = new HashMap<>();
    private Function<V, Collection<?>> iterableSupplier = null;

    //
    // Value methods and functions.  These will be null or empty if this is
    // a static type.
    //

    // The set of Java value types that are proxied by this proxy.
    // Often this will just be type V; but if V is an interface it will
    // be concrete types that implement V.
    private final Set<Class<? extends V>> proxiedTypes = new HashSet<>();

    // The type's initializer
    private NativeFunction initializer = null;

    // The instance methods
    private final Map<String, JoeValueLambda<V>> methods = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a type proxy for value type V.  The name is the script-level
     * name for the type, which need not match V's simple name.
     * @param name The name
     */
    public ProxyType(String name) {
        this.name = name;

        // Every type has a name.
        staticMethod("name", this::_name);
    }

    //-------------------------------------------------------------------------
    // Static Method Implementations


    // TODO: Make sure this gets into the docs for each type.
    private Object _name(Joe joe, Args args) {
        args.exactArity(0, "name()");
        return name();
    }

    //-------------------------------------------------------------------------
    // Type Builders

    /**
     * Declares that this is a static type, with no initializer or
     * instance methods.
     */
    protected void staticType() {
        this.isStatic = true;
    }

    /**
     * Specifies the type proxy of this type's supertype.  In this way
     * a type proxy can inherit the methods (in particular) of its
     * supertype's proxy.
     * @param superProxy The supertype's proxy
     */
    protected void extendsProxy(ProxyType<? super V> superProxy) {
        this.superProxy = superProxy;
    }

    /**
     * Defines a static method for the type.
     * @param name The method name
     * @param joeLambda The lambda that implements the method.
     */
    protected void staticMethod(String name, JoeLambda joeLambda) {
        staticMethods.put(name, new NativeFunction(name, "static method", joeLambda));
    }

    /**
     * Defines a static constant for the type.
     * @param name The constant name
     * @param value The value
     */
    protected void constant(String name, Object value) {
        constants.put(name, value);
    }

    /**
     * Declares that this proxy is a proxy for the given type, which should
     * be a real class (not an interface) that is assignable to the value
     * type.
     *
     * <p>A proxy can proxy any number of related types.</p>
     * @param type The proxied type.
     */
    protected void proxies(Class<? extends V> type) {
        proxiedTypes.add(type);
    }

    /**
     * Defines an initializer for this type.  This defines a global
     * function named after the type.
     * @param joeLambda The lambda that implements the initializer
     */
    protected void initializer(JoeLambda joeLambda) {
        this.initializer = new NativeFunction(name, "initializer", joeLambda);
    }

    /**
     * Defines a way for the proxy to get a list of iterables from
     * the value.
     * @param supplier The iterable supplier.
     */
    @SuppressWarnings("unused")
    protected void iterables(Function<V,Collection<?>> supplier) {
        this.iterableSupplier = supplier;
    }

    /**
     * Defines a method for the type.
     * @param name The method name
     * @param callable The value callable that implements the method.
     */
    protected void method(String name, JoeValueLambda<V> callable) {
        methods.put(name, callable);
    }

    //-------------------------------------------------------------------------
    // JoeType API

    @Override
    public final String name() {
        return name;
    }

    //-------------------------------------------------------------------------
    // JoeValue API

    @Override
    public JoeType type() {
        return TypeType.TYPE;
    }

    @Override
    public boolean hasField(String name) {
        return constants.containsKey(name);
    }

    @Override
    public List<String> getFieldNames() {
        return new ArrayList<>(constants.keySet());
    }

    @Override
    public Object get(String name) {
        if (constants.containsKey(name)) {
            return constants.get(name);
        }

        if (staticMethods.containsKey(name)) {
            return staticMethods.get(name);
        }

        throw new JoeError("Undefined property '" + name + "'.");
    }

    @Override
    public void set(String name, Object value) {
        throw new JoeError("Type " + this.name + " has no settable properties.");
    }

    //-------------------------------------------------------------------------
    // Overridable methods

    /**
     * Returns a stringified value, i.e., a value for display.
     * Defaults to the value's toString().  Subclasses may override.
     * @param joe The engine
     * @param value A value of the proxied type
     * @return The string
     */
    public String stringify(Joe joe, Object value) {
        return value.toString();
    }

    /**
     * Returns true if the value has a field with the given name, and
     * false otherwise.
     *
     * <p>Proxied types are assumed not to have fields, so this always
     * returns false. Subclasses may override.</p>
     * @param value A value of the proxied type
     * @param fieldName The field name
     * @return true or false
     */
    @SuppressWarnings("unused")
    public boolean hasField(Object value, String fieldName) {
        return false;
    }

    /**
     * Returns a list of the names of the value's fields.  The
     * list will be empty if the value has no fields.
     *
     * <p>Proxied types are assumed not to have fields, so this always
     * returns the empty list. Subclasses may override.</p>
     * @param value A value of the proxied type
     * @return The list
     */
    @SuppressWarnings("unused")
    public List<String> getFieldNames(Object value) {
        return List.of();
    }

    /**
     * Gets the value of the named property.  Throws an
     * "Undefined property" error if there is no such property.
     *
     * <p>Proxied types are assumed not to have fields, so this only
     * looks for method properties. Subclasses may override.</p>
     * @param value A value of the proxied type
     * @param propertyName The property name
     * @return The property value
     */
    @SuppressWarnings("unused")
    public Object get(Object value, String propertyName) {
        var method = bind(value, propertyName);

        if (method != null) {
            return method;
        } else {
            throw new JoeError("Undefined property '" +
                propertyName + "'.");
        }
    }

    /**
     * Sets the value of the named field.
     *
     * <p>Proxied types are assumed not to have fields, so this always
     * throws a JoeError. Subclasses may override.</p>
     * @param value A value of the proxied type
     * @param fieldName The field name
     * @param other The value to
     * @return The property value
     */
    @SuppressWarnings("unused")
    public Object set(Object value, String fieldName, Object other) {
        throw new JoeError("Values of type " + name +
            " have no field properties.");
    }

    //-------------------------------------------------------------------------
    // JoeCallable implementation (for type constructor)

    @Override
    public Object call(Joe joe, Args args) {
        if (initializer != null) {
            return initializer.call(joe, args);
        } else if (isStatic) {
            throw new JoeError("Type " + name +
                " is static and cannot be initialized.");
        } else {
            throw new JoeError("Type " + name +
                " cannot be instantiated at the script level.");
        }
    }

    @Override
    public String callableType() {
        return "native initializer";
    }

    @Override
    public String signature() {
        return name + "(...)";
    }

    @Override
    public boolean isScripted() {
        return false;
    }

    //-------------------------------------------------------------------------
    // Iterability

    /**
     * Gets whether or not values of this type are iterable.
     * @return true or false
     */
    public boolean canIterate() {
        return iterableSupplier != null;
    }

    /**
     * If this type is iterable, returns the value's items for iteration.
     * @param value The value
     * @return The items
     */
    @SuppressWarnings("unchecked")
    public Collection<?> getItems(Object value) {
        return iterableSupplier.apply((V)value);
    }

    //-------------------------------------------------------------------------
    // Internal Methods

    /**
     * Given an object, which *must* be assignable to the value type, and a
     * method name, returns a callable that binds the method's callable to
     * the object.
     *
     * <p><b>NOTE:</b> this is intended for use in Interpreter, which will
     * find the proxy via Joe::lookupProxy; hence, this is type-safe.</p>
     * @param value The value
     * @param name The method name
     * @return The bound callable
     * @throws JoeError if the method is not found.
     */
    @SuppressWarnings("unchecked")
    public NativeCallable bind(Object value, String name) {
        var method = methods.get(name);

        if (method != null) {
            return new NativeMethod<>((V) value, name, method);
        } else if (superProxy != null) {
            return superProxy.bind(value, name);
        } else {
            return null;
        }
    }

    //-------------------------------------------------------------------------
    // Public Methods

    /**
     * As a JoeValue, the proxy needs a typeName.  It's simply a
     * "{@code &lt;type&gt;}".
     * @return The name.
     */
    public final String typeName() {
        return "<native type>";
    }

    /**
     * Gets the Java types proxied by this type.  These should be
     * actual classes, not interfaces, that are assignable to the
     * value type.
     * @return The proxied types.
     */
    public final Set<Class<?>> getProxiedTypes() {
        return Collections.unmodifiableSet(proxiedTypes);
    }

    @Override
    public String toString() {
        return name;
    }
}
