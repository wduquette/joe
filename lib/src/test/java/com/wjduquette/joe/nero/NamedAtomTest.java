package com.wjduquette.joe.nero;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.wjduquette.joe.checker.Checker.check;

public class NamedAtomTest extends Ted {
    private final Variable X = new Variable("x");
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
}
