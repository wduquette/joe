package com.wjduquette.joe.nero;

import com.wjduquette.joe.Ted;
import org.junit.Test;

import java.util.List;

import static com.wjduquette.joe.checker.Checker.check;

public class ListAtomTest extends Ted {
    private final Variable X = new Variable("x");
    private final Constant ABC = new Constant("abc");
    private final Wildcard W = new Wildcard("_");

    @Test
    public void testCreation() {
        test("testCreation");
        var atom = new ListAtom("Thing", List.of(X, ABC, W));
        check(atom.relation()).eq("Thing");
        check(atom.terms()).eq(List.of(X, ABC, W));
    }

    @Test
    public void testToString() {
        test("testToString()");
        var atom = new ListAtom("Thing", List.of(X, ABC, W));
        check(atom.toString()).eq("Thing(x, \"abc\", _)");
    }
}
