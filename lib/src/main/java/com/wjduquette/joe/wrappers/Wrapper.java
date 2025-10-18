package com.wjduquette.joe.wrappers;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;

/**
 * A functional interface for converting an arbitrary Object to a
 * value of value type V.  The converter is expected to throw a
 * JoeError on validation failure.
 *
 * @param <V> The value type.
 */
public interface Wrapper<V> {
    /**
     * Returns the argument as a value of type V, doing any necessary
     * validation and type conversions.
     *
     * @param joe The interpreter
     * @param arg The argument
     * @return The converted value
     * @throws JoeError on validation failure.
     */
    V convert(Joe joe, Object arg);
}
