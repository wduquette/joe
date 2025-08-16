package com.wjduquette.joe.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A map of variable bindings.  This is a mini-environment, used by Nero
 * when matching
 * {@link com.wjduquette.joe.nero.Rule Rules} to known
 * {@link com.wjduquette.joe.nero.Fact Facts}, and when matching destructuring
 * {@link com.wjduquette.joe.patterns.Pattern Patterns}.
 *
 */
public class Bindings {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Map<String,Object> map = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an empty set of bindings.
     */
    public Bindings() {
        // nothing to do
    }

    /**
     * Creates a set of bindings, initializing with the other bindings.
     * @param other The other bindings.
     */
    public Bindings(Bindings other) {
        map.putAll(other.map);
    }

    //-------------------------------------------------------------------------
    // API

    public Object get(String name) {
        return map.get(name);
    }

    public void bind(String name, Object value) {
        map.put(name, value);
    }

    /**
     * Unbinds the given variable names.
     * @param names The names
     */
    public void unbindAll(Collection<String> names) {
        for (var name : names) map.remove(name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Bindings bindings = (Bindings) o;
        return map.equals(bindings.map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
