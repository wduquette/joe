package com.wjduquette.joe;

import org.junit.Before;
import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.*;

@SuppressWarnings("ConstantValue")
public class JoeTest extends Ted {
    private Joe engine;

    @Before public void setup() {
        // We need an engine to test these things; either Walker or Clark
        // will do.
        engine = new Joe(Joe.CLARK);
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
        check(engine.stringify(null)).eq("null");
        check(engine.stringify(2.0)).eq("2");
        check(engine.stringify(2.5)).eq("2.5");
        check(engine.stringify("abc")).eq("abc");
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

        check(engine.typeName(5.0)).eq("Number");
    }

    @Test
    public void testIsComplete() {
        test("testIsComplete");

        // Scanner
        var completeString = """
            var a = "abc";
            """;
        var incompleteString = "var a = \"abc;";

        // Parser
        var completeBlock = """
            function dummy() { }
            """;
        var incompleteBlock = """
            function dummy() {
            """;
        var normalError = """
            a = "abc";
            """;
        var errorPlusIncomplete = "a = \"abc;";

        // Walker
        check(engine.isComplete(completeString)).eq(true);
        check(engine.isComplete(incompleteString)).eq(false);

        check(engine.isComplete(completeBlock)).eq(true);
        check(engine.isComplete(incompleteBlock)).eq(false);

        check(engine.isComplete(normalError)).eq(true);
        check(engine.isComplete(errorPlusIncomplete)).eq(false);
    }
}
