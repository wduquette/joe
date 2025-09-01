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

    /**
     * Creates an environment with no enclosing environment.
     */
    public Environment() {
        // Nothing to do
    }

    /**
     * Gets the value of the named global variable.
     * @param name The name
     * @return The value
     */
    public Object getVar(String name) {
        return values.get(name);
    }


    /**
     * Gets the names of the variables declared in the global environment.
     * @return The set.
     */
    public Set<String> getVarNames() {
        return Collections.unmodifiableSet(values.keySet());
    }

    public void dump() {
        System.out.println(this);
        var map = new TreeMap<>(values);
        for (var key : map.keySet()) {
            System.out.printf("  %-20s %s\n", key, map.get(key).toString());
        }
    }


    public void setVar(String name, Object value) {
        values.put(name, value);
    }
}
