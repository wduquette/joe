package com.wjduquette.joe.nero;

import com.wjduquette.joe.Ted;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.wjduquette.joe.checker.Checker.check;

public class EquivalenceTest extends Ted {
    private static Equivalence E = new S2N();

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

    private static class S2N extends Equivalence {
        S2N() {
            super(s -> toNum(s), n -> toStr(n));
        }
    }

    private static double toNum(Object s) {
        // NOTE: in real use, we'd throw a JoeError on error.
        return Double.parseDouble(Objects.toString(s));
    }

    private static String toStr(Object n) {
        // NOTE: in real use we'd throw a JoeError.
        if (n instanceof Double d) {
            return d.toString();
        } else {
            throw new IllegalArgumentException("Bad number");
        }
    }
}
