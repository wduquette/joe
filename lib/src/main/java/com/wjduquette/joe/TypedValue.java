package com.wjduquette.joe;

import java.util.Collection;
import java.util.List;

/**
 * A value bound to its ProxyType.  It is the responsibility
 * of the caller (i.e., the Joe engine) to make sure that the proxy and
 * the value are compatible
 * @param joe The instance of Joe.
 * @param proxy The proxy
 * @param value The value
 */
record TypedValue(Joe joe, ProxyType<?> proxy, Object value)
    implements JoeValue
{
    @Override
    public JoeType type() {
        return proxy;
    }

    @Override
    public String typeName() {
        return proxy.name();
    }

    @Override
    public boolean hasField(String name) {
        // The value is of a proxied type, and so has
        // method properties but not field properties.
        return false;
    }

    @Override
    public List<String> getFieldNames() {
        // The value is of a proxied type, and so has
        // method properties but not field properties.
        return List.of();
    }

    @Override
    public Object get(String name) {
        // The value is of a proxied type, and so has
        // method properties but not field properties.
        var method = proxy.bind(value, name);
        if (method != null) {
            return method;
        } else {
            throw new JoeError("Undefined property '" + name + "'.");
        }
    }

    @Override
    public void set(String name, Object value) {
        // The value is either of an opaque type or a proxied native type.
        // Neither kind of type has field properties.
        throw new JoeError("Values of type " +
            value.getClass().getName() +
            " have no field properties.");
    }

    @Override
    public boolean canIterate() {
        return proxy.canIterate();
    }

    @Override
    public Collection<?> getItems() {
        return proxy.getItems(value);
    }

    @Override public String stringify(Joe joe) {
        return proxy.stringify(joe, value);
    }

    @Override public String toString() {
        return stringify(joe);
    }
}
