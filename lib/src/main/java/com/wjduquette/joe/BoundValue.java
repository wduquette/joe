package com.wjduquette.joe;

import java.util.Collection;

/**
 * A value bound to its TypeProxy, or null.  It is the responsibility
 * of the caller (i.e., the Joe engine) to make sure that the proxy and
 * the value are compatible
 * @param joe The instance of Joe.
 * @param proxy The proxy, or null
 * @param value The value
 */
record BoundValue(Joe joe, TypeProxy<?> proxy, Object value)
    implements JoeObject
{

    @Override
    public String typeName() {
        return proxy != null
            ? proxy.name()
            : value.getClass().getName();
    }

    @Override
    public Object get(String name) {
        if (proxy != null) {
            var method = proxy.bind(value, name);
            if (method != null) {
                return method;
            } else {
                throw new JoeError("Undefined property '" + name + "'.");
            }
        } else {
            throw new JoeError("Values of type " +
                typeName() + " have no gettable properties.");
        }
    }

    @Override
    public void set(String name, Object value) {
        if (proxy != null) {
            proxy.set(name, value);
        } else {
            throw new JoeError("Values of type " +
                value.getClass().getName() +
                " have no settable properties.");
        }
    }

    @Override
    public boolean hasField(String name) {
        return proxy.hasField(name);
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
