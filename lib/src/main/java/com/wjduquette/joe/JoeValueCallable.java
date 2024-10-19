package com.wjduquette.joe;

/**
 * An interface for a native method, defined as part of a `TypeProxy`.
 * @param <V> The value type
 */
public interface JoeValueCallable<V> {
    /**
     * Calls the callable
     * @param value the value to which the method is bound
     * @param joe The Joe interpreter
     * @param args The arguments to the callable
     * @return The callable's result
     * @throws JoeError on any runtime error.
     */
    Object call(V value, Joe joe, Args args);
}
