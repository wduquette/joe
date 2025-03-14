package com.wjduquette.joe;

import com.wjduquette.joe.types.ListValue;

import java.util.Collection;

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
        return proxy != null
            ? proxy.name()
            : value.getClass().getName();
    }

    @Override
    public boolean hasField(String name) {
        // The value is of a proxied type, and so has
        // method properties but not field properties.
        return false;
    }

    @Override
    public JoeList getFieldNames() {
        // The value is of a proxied type, and so has
        // method properties but not field properties.
        return new ListValue();
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
        if (proxy != null) {
            return proxy.stringify(joe, value);
        } else {
            return value.toString();
        }
    }

    @Override public String toString() {
        return stringify(joe);
    }
}
