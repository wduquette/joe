package com.wjduquette.joe;

import java.util.*;

/**
 * A Joe environment, a dictionary of global or package variable names and
 * values.
 */
public class Environment {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The map from names to values.
    protected final Map<String, Object> values = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an environment.
     */
    public Environment() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // Environment API

    /**
     * Returns true if the environment contains the named variable,
     * and false otherwise.
     * @param name The name
     * @return true or false
     */
    @SuppressWarnings("unused")
    public boolean hasVariable(String name) {
        return values.containsKey(name);
    }

    /**
     * Gets the value of the named variable in this environment.
     * @param name The name
     * @return The value
     */
    public Object getVariable(String name) {
        return values.get(name);
    }

    /**
     * Sets the value of the named variable in this environment.
     * @param name The name
     * @param value The value
     */
    public void setVariable(String name, Object value) {
        values.put(name, value);
    }

    /**
     * Gets the names of the variables declared in the global environment.
     * @return The set.
     */
    public Set<String> getVariableNames() {
        return Collections.unmodifiableSet(values.keySet());
    }

    /**
     * Dump the contents of this environment as a string, for debugging.
     */
    public String dump() {
        var buff = new StringBuilder();
        buff.append(this);
        var map = new TreeMap<>(values);
        for (var key : map.keySet()) {
            buff.append(String.format(
                "  %-20s %s\n", key, map.get(key).toString()));
        }
        return buff.toString().stripTrailing();
    }
}
