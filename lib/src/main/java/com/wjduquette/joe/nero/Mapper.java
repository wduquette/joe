package com.wjduquette.joe.nero;

/**
 * A mapping function for use with the mapsTo/f,a,b predicate.  Maps
 * a value of type A to a value of type B.
 */
public interface Mapper {
    /**
     * Given a value of type A, produce a value of type B.  Return
     * null or throw an error on conversion failure.
     * @param a The input of type A
     * @return the output of type B
     */
    Object a2b(Object a);
}
