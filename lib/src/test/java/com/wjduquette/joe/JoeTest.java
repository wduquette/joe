package com.wjduquette.joe;

import org.junit.Before;
import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.*;

@SuppressWarnings("ConstantValue")
public class JoeTest extends Ted {
    private Joe joe;

    @Before public void setup() {
        this.joe = new Joe();
    }

    @Test
    public void testIsTruthy() {
        test("testIsTruthy");
        check(Joe.isTruthy(true)).eq(true);
        check(Joe.isTruthy(1.0)).eq(true);
        check(Joe.isTruthy("abc")).eq(true);

        check(Joe.isTruthy(false)).eq(false);
        check(Joe.isTruthy(null)).eq(false);
    }

    @Test
    public void testIsEqual() {
        test("testIsEqual");
        check(Joe.isEqual(null, null)).eq(true);
        check(Joe.isEqual(1.0, 1.0)).eq(true);
        check(Joe.isEqual("abc", "abc")).eq(true);

        check(Joe.isEqual(null,  "abc")).eq(false);
        check(Joe.isEqual("abc", null)).eq(false);
        check(Joe.isEqual("abc", "def")).eq(false);
    }

    @Test
    public void testStringify() {
        test("testStringify");
        check(joe.stringify(null)).eq("null");
        check(joe.stringify(2.0)).eq("2");
        check(joe.stringify(2.5)).eq("2.5");
        check(joe.stringify("abc")).eq("abc");
    }

    @Test
    public void testCodify() {
        test("testCodify");
        check(joe.codify(null)).eq("null");
        check(joe.codify(2.0)).eq("2");
        check(joe.codify(2.5)).eq("2.5");
        check(joe.codify(true)).eq("true");
        check(joe.codify("abc")).eq("\"abc\"");
    }

    @Test
    public void testEscape() {
        test("testEscape");
        check(Joe.escape("-abcd-")).eq("-abcd-");
        check(Joe.escape("-\\-")).eq("-\\\\-");
        check(Joe.escape("-\t-")).eq("-\\t-");
        check(Joe.escape("-\b-")).eq("-\\b-");
        check(Joe.escape("-\n-")).eq("-\\n-");
        check(Joe.escape("-\f-")).eq("-\\f-");
        check(Joe.escape("-\"-")).eq("-\\\"-");
        check(Joe.escape("-→-")).eq("-\\u2192-");
    }

    @Test
    public void testTypeName() {
        test("testTypeName");

        check(joe.typeName(5.0)).eq("Number");
    }
}
