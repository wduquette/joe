package com.wjduquette.joe;

import java.util.*;

/**
 * A proxy for a native Java type.  The proxy implements all required
 * metadata and services for the type.
 * @param <V> The native value type
 */
public class TypeProxy<V> implements JoeCallable {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final String typeName;
    private final Set<Class<? extends V>> proxiedTypes = new HashSet<>();
    private NativeFunction initializer = null;
    private final Map<String, JoeValueCallable<V>> methods = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a type proxy for value type V.  The name is the script-level
     * name for the type, which need not match V's simple name.
     * @param name The name
     */
    public TypeProxy(String name) {
        this.typeName = name;
    }

    //-------------------------------------------------------------------------
    // Type Builders

    /**
     * Declares that this proxy is a proxy for the given type, which should
     * be a real class (not an interface) that is assignable to the value
     * type.
     *
     * A proxy can proxy any number of related types.
     * @param type The proxied type.
     */
    public void proxies(Class<? extends V> type) {
        proxiedTypes.add(type);
    }

    /**
     * Defines an initializer for this type.  This defines a global
     * function named after the type.
     * @param callable The callable
     */
    public void initializer(JoeCallable callable) {
        this.initializer = new NativeFunction(typeName, callable);
    }

    /**
     * Defines a method for the type.
     * @param name The method name
     * @param callable The value callable that implements the method.
     */
    public void method(String name, JoeValueCallable<V> callable) {
        methods.put(name, callable);
    }

    //-------------------------------------------------------------------------
    // Overridable methods

    /**
     * Returns a stringified value, i.e., a value for display.
     * Defaults to the value's toString().
     * @param joe The engine
     * @param value The value
     * @return The string
     */
    public String stringify(Joe joe, V value) {
        return value.toString();
    }

    /**
     * Returns a codified value, i.e., a value as it could be entered
     * in code, or a "<...>" token otherwise.
     * Defaults to the value's toString().
     * @param joe The engine
     * @param value The value
     * @return The string
     */
    public String codify(Joe joe, V value) {
        return value.toString();
    }

    //-------------------------------------------------------------------------
    // Initializer implementation

    @Override
    public Object call(Joe joe, List<Object> args) {
        if (initializer != null) {
            return initializer.call(joe, args);
        } else if (methods.isEmpty()) {
            throw new JoeError("Type " + typeName +
                " is static and cannot be initialized.");
        } else {
            throw new JoeError("Type " + typeName +
                " cannot be instantiated at the script level.");
        }
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
    JoeCallable bind(Object value, String name) {
        var method = methods.get(name);

        // TODO: Consider allowing method chaining
        if (method != null) {
            return (joe, args) -> method.call((V)value, joe, args);
        } else {
            throw new JoeError("Undefined property '" + name + "'.");
        }
    }

    //-------------------------------------------------------------------------
    // Public Methods

    /**
     * Gets the type's script-level name, which might not match its
     * Java name.
     * @return The name
     */
    public final String getTypeName() {
        return typeName;
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
}
