package com.wjduquette.joe.wrappers;

/**
 * A functional interface for converting the internal (i.e., native)
 * representation of a value to its Joe representation, e.g, converting
 * JavaFX callbacks back into Joe callables.
 *
 * @param <V> The value type
 */
public interface Unwrapper<V> {
    /**
     * Unwraps a value of type V to its Joe representation
     *
     * @param value The wrapped value
     * @return The unwrapped value
     */
    Object unwrap(V value);
}
