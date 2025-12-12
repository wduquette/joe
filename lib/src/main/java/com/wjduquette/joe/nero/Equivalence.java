package com.wjduquette.joe.nero;

import java.util.function.Function;

/**
 * An equivalence is a bijection between two types, e.g., between a value type
 * and its string representation.  It is used by the equivalent/3 built-in
 * predicate to check for equivalence and do conversions.
 *
 * <p>Ideally it will be a strict bijection: every value of a maps to a unique
 * value of b and vice-versa.  When dealing with an equivalence between a
 * value type and its string representation, it's allowable for a single value
 * to have multiple string representations provided that (a) a given string
 * always maps to the same value, and (b) a given value will always map to the
 * same string.</p>
 *
 * <p>For example, given an Equivalence between numbers and their
 * string representations, "1", "1.0", "1e0" all map to the double 1.0; the
 * double 1.0 always maps to "1".
 * </p>
 */
public class Equivalence {
    //-------------------------------------------------------------------------
    // Instance variables

    private final Function<Object,Object> a2b;
    private final Function<Object,Object> b2a;

    //-------------------------------------------------------------------------
    // Constructor

    public Equivalence(Function<Object,Object> a2b, Function<Object,Object> b2a) {
        this.a2b = a2b;
        this.b2a = b2a;
    }

    //-------------------------------------------------------------------------
    // API

    public Object a2b(Object a) {
        return a2b.apply(a);
    }

    public Object b2a(Object b) {
        return b2a.apply(b);
    }

    public boolean isEquivalent(Object a, Object b) {
        try {
            return a2b.apply(a).equals(b) || b2a.apply(b).equals(a);
        } catch (Exception ex) {
            return false;
        }
    }
}
