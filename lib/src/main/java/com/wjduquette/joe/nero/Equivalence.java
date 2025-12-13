package com.wjduquette.joe.nero;

import com.wjduquette.joe.Keyword;

/**
 * An equivalence is a bijection between two types A and B, e.g., between a
 * value type and its string representation.  It is used by the equivalent/3
 * built-in predicate to check for equivalence and do conversions.
 *
 * <p>The methods are written in terms of Object since predicate values
 * are passed in as Objects.</p>
 *
 * <p>Ideally the equivalence will be a strict bijection: every value of A
 * maps to a unique value of B and <i>vice versa</i>.  However, we allow for
 * the case where A is the string representation of B and there are
 * multiple valid strings for each value of B.  Given an Equivalence between
 * numeric strings and doubles, for example, "1", "1.0", and "1e0" all map to
 * the double 1.0, while the double 1.0 always maps to "1".
 * </p>
 *
 * <p>A conversion should return null to indicate that the conversion couldn't
 * be performed rather than throwing an error. The equivalent/3 predicate
 * will fail to match if a conversion returns null.</p>
 */
public abstract class Equivalence {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Keyword keyword;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Initializes the equivalence with its identifying keyword.
     * @param keyword The keyword
     */
    public Equivalence(Keyword keyword) {
        this.keyword = keyword;
    }

    //-------------------------------------------------------------------------
    // API

    /**
     * Gets the equivalence's identifying keyword.
     * @return The keyword
     */
    public Keyword keyword() {
        return keyword;
    }

    /**
     * Tries to convert a value of type A to a value of type B.  The method
     * should return null if the input is not a value of type A or cannot be
     * converted to type B.
     * @param a the value of type A
     * @return the value of type B, or null on failure
     */
    abstract public Object a2b(Object a);

    /**
     * Convert a value of type B to a value of type A.  The method should
     * return null if the input is not a value of type B or cannot be
     * converted to type A.
     * @param b the value of type B
     * @return the value of type A, or null on failure
     */
    abstract public Object b2a(Object b);

    /**
     * Checks whether a and b are equivalent values for this equivalence.
     * Values a and b are judged to be equivalent if the equivalence converts
     * a to b or b to a; this allows for the case where multiple strings can
     * be converted to a single value.  The two values are NOT equivalent if
     * either conversion returns null.
     * @param a The value of type A
     * @param b The value of type B
     * @return true or false
     */
    public boolean isEquivalent(Object a, Object b) {
        var toB = a2b(a);
        var toA = b2a(b);
        return toA != null && toB != null && (
            toB.equals(b) || toA.equals(a)
        );
    }
}
