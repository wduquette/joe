package com.wjduquette.joe.nero;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.wjduquette.joe.checker.Checker.check;

public class NamedAtomTest extends Ted {
    private final Bindings EMPTY = new Bindings();
    private final Variable X = new Variable("x");
    private final Variable Y = new Variable("y");
    private final Constant ABC = new Constant("abc");
    private final Wildcard W = new Wildcard("_");

    @Test
    public void testCreation() {
        test("testCreation");
        var map = new LinkedHashMap<String,Term>();
        map.put("f0", X);
        map.put("f1", ABC);
        map.put("f2", W);
        var atom = new NamedAtom("Thing", map);
        check(atom.relation()).eq("Thing");
        check(atom.terms()).eq(Map.of("f0", X, "f1", ABC, "f2", W));
    }

    @Test
    public void testToString() {
        test("testToString()");
        var map = new LinkedHashMap<String,Term>();
        map.put("f0", X);
        map.put("f1", ABC);
        map.put("f2", W);
        var atom = new NamedAtom("Thing", map);
        check(atom.toString()).eq("Thing(f0: x, f1: \"abc\", f2: _)");
    }

    @Test
    public void testMatch_relationMismatch() {
        var map = new LinkedHashMap<String,Term>();
        map.put("f0", X);
        map.put("f1", ABC);
        map.put("f2", W);
        var atom = new NamedAtom("Thing", map);

        var fact = new OrderedFieldFact("Gizmo", List.of(1, "abc", 2));
        check(atom.matches(fact, EMPTY)).eq(null);
    }

    @Test
    public void testMatch_constantMismatch() {
        var map = new LinkedHashMap<String,Term>();
        map.put("f0", X);
        map.put("f1", ABC);
        map.put("f2", W);
        var atom = new NamedAtom("Thing", map);

        var fact = new OrderedFieldFact("Thing", List.of(1, "def", 2));
        check(atom.matches(fact, EMPTY)).eq(null);
    }

    @Test
    public void testMatch_bindingMismatch() {
        var map = new LinkedHashMap<String,Term>();
        map.put("f0", X);
        map.put("f1", ABC);
        map.put("f2", W);
        var atom = new NamedAtom("Thing", map);

        var fact = new OrderedFieldFact("Thing", List.of(1, "abc", 2));
        var bindings = new Bindings();
        bindings.put(X, 0);
        check(atom.matches(fact, bindings)).eq(null);
    }

    @Test
    public void testMatch_newBinding() {
        var map = new LinkedHashMap<String,Term>();
        map.put("f0", X);
        map.put("f1", ABC);
        map.put("f2", W);
        var atom = new NamedAtom("Thing", map);

        var fact = new OrderedFieldFact("Thing", List.of(1, "abc", 2));

        var expected = new Bindings();
        expected.put(X, 1);
        check(atom.matches(fact, EMPTY)).eq(expected);
    }

    @Test
    public void testMatch_retainedBinding() {
        var map = new LinkedHashMap<String,Term>();
        map.put("f0", X);
        map.put("f1", ABC);
        map.put("f2", W);
        var atom = new NamedAtom("Thing", map);

        var fact = new OrderedFieldFact("Thing", List.of(1, "abc", 2));

        var expected = new Bindings();
        expected.put(X, 1);
        check(atom.matches(fact, expected)).eq(expected);
    }

    @Test
    public void testMatch_retainedAndNew() {
        var map = new LinkedHashMap<String,Term>();
        map.put("f0", X);
        map.put("f1", ABC);
        map.put("f2", Y);
        var atom = new NamedAtom("Thing", map);

        var fact = new OrderedFieldFact("Thing", List.of(1, "abc", 2));
        var given  = new Bindings();
        given.put(X, 1);
        var expected  = new Bindings(given);
        expected.put(Y, 2);
        check(atom.matches(fact, given)).eq(expected);
    }

    @Test
    public void testMatch_boundAndMatched_good() {
        var map = new LinkedHashMap<String,Term>();
        map.put("f0", X);
        map.put("f1", ABC);
        map.put("f2", X);
        var atom = new NamedAtom("Thing", map);

        var fact = new OrderedFieldFact("Thing", List.of(1, "abc", 1));

        var expected  = new Bindings();
        expected.put(X, 1);
        check(atom.matches(fact, EMPTY)).eq(expected);
    }

    @Test
    public void testMatch_boundAndMatched_bad() {
        var map = new LinkedHashMap<String,Term>();
        map.put("f0", X);
        map.put("f1", ABC);
        map.put("f2", X);
        var atom = new NamedAtom("Thing", map);

        var fact = new OrderedFieldFact("Thing", List.of(1, "abc", 2));
        check(atom.matches(fact, EMPTY)).eq(null);
    }
}
