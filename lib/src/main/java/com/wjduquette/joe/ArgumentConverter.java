package com.wjduquette.joe;

/**
 * A functional interface for converting an arbitrary Object to a
 * value of property type P.  The converter is expected to throw a
 * JoeError on conversion failure.
 *
 * @param <P> The property type.
 */
public interface ArgumentConverter<P> {
    /**
     * Converts the argument to a value of type P.
     *
     * @param joe The interpreter
     * @param arg The argument
     * @return The converted value
     * @throws JoeError on conversion failure.
     */
    P convert(Joe joe, Object arg);
}
