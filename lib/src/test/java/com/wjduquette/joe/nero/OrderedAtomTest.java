package com.wjduquette.joe.nero;

import org.junit.Test;

import java.util.List;

import static com.wjduquette.joe.checker.Checker.check;

public class OrderedAtomTest extends Ted {
    private final Bindings EMPTY = new Bindings();
    private final Variable X = new Variable("x");
    private final Variable Y = new Variable("y");
    private final Constant ABC = new Constant("abc");
    private final Wildcard W = new Wildcard("_");

    @Test
    public void testCreation() {
        test("testCreation");
        var atom = new OrderedAtom("Thing", List.of(X, ABC, W));
        check(atom.relation()).eq("Thing");
        check(atom.terms()).eq(List.of(X, ABC, W));
    }

    @Test
    public void testToString() {
        test("testToString()");
        var atom = new OrderedAtom("Thing", List.of(X, ABC, W));
        check(atom.toString()).eq("Thing(x, \"abc\", _)");
    }

    @Test
    public void testMatch_relationMismatch() {
        var atom = new OrderedAtom("Thing", List.of(X, ABC, W));
        var fact = new ListFact("Gizmo", List.of(1, "abc", 2));
        check(atom.matches(fact, EMPTY)).eq(null);
    }

    @Test
    public void testMatch_constantMismatch() {
        var atom = new OrderedAtom("Thing", List.of(X, ABC, W));
        var fact = new ListFact("Thing", List.of(1, "def", 2));
        check(atom.matches(fact, EMPTY)).eq(null);
    }

    @Test
    public void testMatch_bindingMismatch() {
        var atom = new OrderedAtom("Thing", List.of(X, ABC, W));
        var fact = new ListFact("Thing", List.of(1, "abc", 2));
        var bindings = new Bindings();
        bindings.put(X, 0);
        check(atom.matches(fact, bindings)).eq(null);
    }

    @Test
    public void testMatch_newBinding() {
        var atom = new OrderedAtom("Thing", List.of(X, ABC, W));
        var fact = new ListFact("Thing", List.of(1, "abc", 2));
        var expected = new Bindings();
        expected.put(X, 1);
        check(atom.matches(fact, EMPTY)).eq(expected);
    }

    @Test
    public void testMatch_retainedBinding() {
        var atom = new OrderedAtom("Thing", List.of(X, ABC, W));
        var fact = new ListFact("Thing", List.of(1, "abc", 2));
        var expected = new Bindings();
        expected.put(X, 1);
        check(atom.matches(fact, expected)).eq(expected);
    }

    @Test
    public void testMatch_retainedAndNew() {
        var atom = new OrderedAtom("Thing", List.of(X, ABC, Y));
        var fact = new ListFact("Thing", List.of(1, "abc", 2));
        var given  = new Bindings();
        given.put(X, 1);
        var expected  = new Bindings(given);
        expected.put(Y, 2);
        check(atom.matches(fact, given)).eq(expected);
    }

    @Test
    public void testMatch_boundAndMatched_good() {
        var atom = new OrderedAtom("Thing", List.of(X, ABC, X));
        var fact = new ListFact("Thing", List.of(1, "abc", 1));
        var expected  = new Bindings();
        expected.put(X, 1);
        check(atom.matches(fact, EMPTY)).eq(expected);
    }

    @Test
    public void testMatch_boundAndMatched_bad() {
        var atom = new OrderedAtom("Thing", List.of(X, ABC, X));
        var fact = new ListFact("Thing", List.of(1, "abc", 2));
        check(atom.matches(fact, EMPTY)).eq(null);
    }
}
