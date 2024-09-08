package com.wjduquette.joe;

/**
 * An interface for a native method, defined as part of a `TypeProxy`.
 * @param <V> The value type
 */
public interface JoeValueCallable<V> {
    Object call(V value, Joe joe, ArgQueue args);
}
