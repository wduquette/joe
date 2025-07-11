package com.wjduquette.joe;

import com.wjduquette.joe.nero.Fact;

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
    //-------------------------------------------------------------------------
    // JoeValue API

    @Override
    public JoeType type() {
        return proxy;
    }

    @Override
    public List<String> getFieldNames() {
        return proxy.getFieldNames(value);
    }

    @Override
    public Object get(String name) {
        return proxy.get(joe, value, name);
    }

    @Override
    public void set(String name, Object other) {
        proxy.set(value, name, other);
    }

    @Override
    public boolean isFact() {
        return proxy.isFact(joe, value);
    }

    @Override
    public Fact toFact() {
        return proxy.toFact(joe, value);
    }

    @Override
    public boolean canIterate() {
        return proxy.canIterate();
    }

    @Override
    public Collection<?> getItems() {
        return proxy.getItems(joe, value);
    }

    @Override public String stringify(Joe joe) {
        return proxy.stringify(joe, value);
    }

    @Override public String toString() {
        return stringify(joe);
    }
}
