package com.wjduquette.joe.nero;

import com.wjduquette.joe.Ted;
import com.wjduquette.joe.util.Bindings;
import org.junit.Test;

import java.util.List;

import static com.wjduquette.joe.checker.Checker.*;

public class TermTest extends Ted {
    private final Bindings EMPTY = new Bindings();

    @Test
    public void testConstant() {
        test("testConstant()");
        var c = new Constant("abc");

        // The type itself
        check(c.value()).eq("abc");

        // String rep must quote strings
        check(c.toString()).eq("\"abc\""); // quoted!

        // The term's value is just its value
        check(Term.toValue(c, EMPTY)).eq("abc");
    }

    @Test
    public void testListTerm() {
        test("testListTerm()");
        var c = new Constant("abc");
        var x = new Variable("x");
        var t = new ListTerm(List.of(c, x));
        var bindings = new Bindings();
        bindings.bind("x", "xyz");

        // String rep is the literal
        check(t.toString()).eq("[\"abc\", x]");

        // The value is the list of the term values.
        check(Term.toValue(t, EMPTY)).eq(listOf("abc", null));
        check(Term.toValue(t, bindings)).eq(listOf("abc", "xyz"));
    }

    @Test
    public void testMapTerm() {
        test("testMapTerm()");
        var k1 = new Constant("a");
        var v1 = new Variable("x");
        var k2 = new Constant("b");
        var v2 = new Variable("y");
        var t = new MapTerm(List.of(k1, v1, k2, v2));
        var bindings = new Bindings();
        bindings.bind("x", "this");
        bindings.bind("y", "that");

        // String rep is the literal
        check(new MapTerm(List.of()).toString()).eq("{:}");
        check(t.toString()).eq("{\"a\": x, \"b\": y}");

        // The value is the map produces from the keys and values
        check(Term.toValue(t, EMPTY)).eq(mapOf("a", null, "b", null));
        check(Term.toValue(t, bindings)).eq(mapOf("a", "this", "b", "that"));
    }

    @Test
    public void testSetTerm() {
        test("testSetTerm()");
        var c = new Constant("abc");
        var x = new Variable("x");
        var t = new SetTerm(List.of(c, x));
        var bindings = new Bindings();
        bindings.bind("x", "xyz");

        // String rep is the literal
        check(t.toString()).eq("{\"abc\", x}");

        // The value is the list of the term values.
        check(Term.toValue(t, EMPTY)).eq(setOf("abc", null));
        check(Term.toValue(t, bindings)).eq(setOf("abc", "xyz"));
    }

    @Test
    public void testVariable() {
        test("testVariable()");
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
        test("testWildcard()");
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
