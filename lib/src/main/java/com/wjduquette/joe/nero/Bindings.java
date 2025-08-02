package com.wjduquette.joe.nero;

import java.util.HashMap;
import java.util.Map;

/**
 * A map of variable bindings, used while matching
 * {@link Atom BodyAtoms} against {@link Fact Facts}.
 */
public class Bindings extends HashMap<String,Object> {
    /**
     * Creates an empty set of bindings.
     */
    public Bindings() {
        super();
    }

    /**
     * Creates a set of bindings, initializing with the other bindings.
     * @param other The other bindings.
     */
    public Bindings(Map<String,Object> other) {
        super(other);
    }
}
