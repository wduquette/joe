package com.wjduquette.joe.types;

import com.wjduquette.joe.JoeMap;
import com.wjduquette.joe.JoeSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * The standard Set class for sets created in Joe code.
 */
public class SetValue extends HashSet<Object> implements JoeSet {
    /**
     * Creates an empty set.
     */
    public SetValue() {
        // Nothing to do.
    }

    /**
     * Creates a new set with the content of the other collection.
     * @param other The other collection
     */
    public SetValue(Collection<?> other) {
        addAll(other);
    }
}
