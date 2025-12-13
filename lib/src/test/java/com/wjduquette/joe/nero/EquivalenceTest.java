package com.wjduquette.joe.nero;

import com.wjduquette.joe.Ted;
import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.check;

public class EquivalenceTest extends Ted {
    private final Equivalence E = new Equivalence(this::s2n, this::n2s);

    @Test
    public void testConversions() {
        test("testConversions");
        check(E.a2b("1")).eq(1.0);
        check(E.a2b("1.0")).eq(1.0);
        check(E.a2b("1.000")).eq(1.0);
        check(E.a2b("1e0")).eq(1.0);
        check(E.b2a(1.0)).eq("1.0");
        check(E.b2a(1e0)).eq("1.0");
    }

    @Test
    public void testIsEquivalent() {
        test("testIsEquivalent");
        check(E.isEquivalent("1", 1.0)).eq(true);
        check(E.isEquivalent("1.0", 1.0)).eq(true);
        check(E.isEquivalent("1.000", 1.0)).eq(true);
        check(E.isEquivalent("1e0000", 1.0)).eq(true);
        check(E.isEquivalent("1", 2.0)).eq(false);
    }

    private Object s2n(Object a) {
        try {
            if (a instanceof String s) return Double.parseDouble(s);
        } catch (Exception ex) {
            // Nothing to do.
        }
        return null;
    }

    private Object n2s(Object b) {
        if (b instanceof Double d) {
            return d.toString();
        } else {
            return null;
        }
    }
}
