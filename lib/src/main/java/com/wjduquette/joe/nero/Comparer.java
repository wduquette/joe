package com.wjduquette.joe.nero;

/**
 * A comparison function for use with values of arbitrary types.
 * Returns an Integer given two values.  If they have the expected type,
 * returns -1, 0, or 1 in the usual way.  Otherwise, the comparison
 * returns null.
 */
public interface Comparer {
    /**
     * Returns -1, 0, or 1 if the values are comparable to each other, and
     * null otherwise.
     * @param a The first value
     * @param b The second value
     * @return -1, 0, 1, or null
     */
    Integer compare(Object a, Object b);
}
