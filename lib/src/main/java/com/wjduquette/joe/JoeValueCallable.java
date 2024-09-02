package com.wjduquette.joe;

import java.util.List;

/**
 * An interface for a native method, defined as part of a `TypeProxy`.
 * @param <V> The value type
 */
public interface JoeValueCallable<V> {
    Object call(V value, Joe joe, List<Object> args);
}
