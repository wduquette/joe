package com.wjduquette.joe.nero;

import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.*;

public class TermTest extends Ted {
    private final Bindings EMPTY = new Bindings();

    @Test
    public void testConstant() {
        var c = new Constant("abc");

        // The type itself
        check(c.value()).eq("abc");

        // String rep must quote strings
        check(c.toString()).eq("\"abc\""); // quoted!

        // The term's value is just its value
        check(Term.toValue(c, EMPTY)).eq("abc");
    }

    @Test
    public void testVariable() {
        var v = new Variable("x");
        var bindings = new Bindings();
        bindings.bind("x", "abc");

        // The type itself
        check(v.name()).eq("x");

        // String rep is the variable name
        check(v.toString()).eq("x");

        // The value is whatever is in the bindings
        check(Term.toValue(v, EMPTY)).eq(null);
        check(Term.toValue(v, bindings)).eq("abc");
    }

    @Test
    public void testWildcard() {
        var w = new Wildcard("_");

        // The type itself
        check(w.name()).eq("_");

        // String rep is the wildcard name
        check(w.toString()).eq("_");

        // Wildcard has no value.
        checkThrow(() -> Term.toValue(w, EMPTY))
            .containsString("toValue is unsupported for body term: Wildcard '_'.");
    }
}
