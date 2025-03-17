package com.wjduquette.joe;

import java.util.Collection;
import java.util.List;

/**
 * A `JoeValue` for `null`.
 */
class NullValue implements JoeValue {
    @Override
    public JoeType type() {
        return null;
    }

    @Override
    public String typeName() {
        return "null";
    }

    @Override
    public boolean hasField(String name) {
        return false;
    }

    @Override
    public List<String> getFieldNames() {
        return List.of();
    }

    @Override
    public Object get(String name) {
        throw new JoeError("`null` has no properties.");
    }

    @Override
    public void set(String name, Object value) {
        throw new JoeError("`null` has no properties.");
    }

    @Override
    public Collection<?> getItems() {
        throw new JoeError("`null` is not iterable.");
    }

    @Override public String stringify(Joe joe) {
        return "null";
    }

    @Override public String toString() {
        return "null";
    }
}
