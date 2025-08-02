package com.wjduquette.joe.nero;

import java.util.HashMap;
import java.util.Map;

/**
 * A map of variable bindings, used while matching
 * {@link Atom BodyAtoms} against {@link Fact Facts}.
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
}
