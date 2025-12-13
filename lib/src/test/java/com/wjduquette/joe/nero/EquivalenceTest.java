package com.wjduquette.joe.nero;

import com.wjduquette.joe.Keyword;
import com.wjduquette.joe.Ted;
import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.check;

public class EquivalenceTest extends Ted {
    private final static Equivalence E = new S2N();

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
        public S2N() { super(new Keyword("s2n")); }
        @Override
        public Object a2b(Object a) {
            try {
                if (a instanceof String s) return Double.parseDouble(s);
            } catch (Exception ex) {
                // Nothing to do.
            }
            return null;
        }

        @Override
        public Object b2a(Object b) {
            // NOTE: in real use we'd throw a JoeError.
            if (b instanceof Double d) {
                return d.toString();
            } else {
                return null;
            }
        }
    }
}
