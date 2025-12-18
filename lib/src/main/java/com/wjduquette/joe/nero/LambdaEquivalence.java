package com.wjduquette.joe.nero;

import com.wjduquette.joe.Keyword;

import java.util.function.Function;

/**
 * An {@link LambdaEquivalence} defined by external lambdas {@code a2b} and
 * {@code b2a}.
 *
 * <p>Both functions deal with Objects, because that's what Nero expects.</p>
 *
 * <p>Function {@code a2b} expects a value of type A; it should return a value
 * of type B, or null if the input is not an A or if the
 * conversion fails.</p>
 *
 * <p>Similarly, function {@code b2a} expects a value of type B; it should
 * return a value of type A, or null if the input is not a B, or if the
 * conversion fails.</p>
 */
public final class LambdaEquivalence extends Equivalence {
    //------------------------------------------------------------------------
    // Instance Variables

    private final Function<Object,Object> a2b;
    private final Function<Object,Object> b2a;

    //------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an equivalence given two conversion lambdas.  Lambda
     * {@code a2b} should convert a value of type A to type B, returning
     * null on any failure, including being passing something other than a
     * value of type A.  Similarly, {@code b2a} should return null on
     * any failure.
     *
     * <p>Lambda exceptions are handled as though the lambda returned null.</p>
     * @param keyword The equivalence's identifying keyword.
     * @param a2b conversion from type A to type B
     * @param b2a conversion from type B to type A
     */
    public LambdaEquivalence(
        Keyword keyword,
        Function<Object,Object> a2b,
        Function<Object,Object> b2a
    ) {
        super(keyword);
        this.a2b = a2b;
        this.b2a = b2a;
    }

    //------------------------------------------------------------------------
    // AbstractEquivalence API

    @Override public Object a2b(Object a) {
        try { return a2b.apply(a); } catch (Exception ex) { return null; }
    }

    @Override public Object b2a(Object b) {
        try { return b2a.apply(b); } catch (Exception ex) { return null; }
    }
}
