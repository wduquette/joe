package com.wjduquette.joe.util;

import java.util.*;

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

    // Use a LinkedHashMap to preserve the order of binding.  This is
    // particularly important for destructuring patterns in the Clark VM.
    private final Map<String,Object> map = new LinkedHashMap<>();

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

    /**
     * Gets a variable's value given its name.
     * @param name The variable name
     * @return The value, or null if !hasBinding(name)
     */
    public Object get(String name) {
        return map.get(name);
    }

    /**
     * Binds the named variable to the value.
     * @param name The variable name
     * @param value The value.
     */
    public void bind(String name, Object value) {
        map.put(name, value);
    }

    /**
     * Gets whether or not there is a binding for the named variable.
     * Use this rather than checking that `get(name) == null`; a variable
     * can be bound to `null`.
     * @param name The name
     * @return true or false
     */
    public boolean hasBinding(String name) {
        return map.containsKey(name);
    }

    /**
     * Returns true if no variables are bound, and false otherwise.
     * @return true or false
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Unbinds the given variable names.
     * @param names The names
     */
    public void unbindAll(Collection<String> names) {
        for (var name : names) map.remove(name);
    }

    /**
     * Gets the bindings as read-only map.
     * @return The map
     */
    public Map<String,Object> asMap() {
        return Collections.unmodifiableMap(map);
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
