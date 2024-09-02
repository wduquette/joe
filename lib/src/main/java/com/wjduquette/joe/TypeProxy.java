package com.wjduquette.joe;

import java.util.*;

/**
 * A proxy for a native Java type.  The proxy implements all required
 * metadata and services for the type.
 * @param <V> The native value type
 */
public class TypeProxy<V> {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final String typeName;
    private final Set<Class<? extends V>> proxiedTypes = new HashSet<>();
    private final Map<String, JoeValueCallable<V>> methods = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    public TypeProxy(String name) {
        this.typeName = name;
    }


    //-------------------------------------------------------------------------
    // Type Builders

    public void proxies(Class<? extends V> type) {
        proxiedTypes.add(type);
    }

    public void method(String name, JoeValueCallable<V> callable) {
        methods.put(name, callable);
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

    public String getTypeName() {
        return typeName;
    }

    public Set<Class<?>> getProxiedTypes() {
        return Collections.unmodifiableSet(proxiedTypes);
    }

}
