package com.wjduquette.joe;

import java.util.Collection;

/**
 * A value, paired with its TypeProxy, or null.  It is the responsibility
 * of the caller (i.e., the Joe engine) to make sure that the proxy and
 * the value are compatible
 * @param joe The instance of Joe.
 * @param proxy The proxy, or null
 * @param value The value
 */
record ProxiedValue(Joe joe, TypeProxy<?> proxy, Object value)
    implements JoeObject
{
    @Override
    public Object get(String name) {
        if (proxy != null) {
            return proxy.bind(value, name);
        } else {
            throw new JoeError("Values of type " +
                value.getClass().getName() +
                " have no gettable properties.");
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
    public boolean canIterate() {
        return proxy.canIterate();
    }

    @Override
    public Collection<?> getItems() {
        return proxy.getItems(value);
    }
}
