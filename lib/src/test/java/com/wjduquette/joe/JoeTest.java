package com.wjduquette.joe;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class JoeTest extends Ted {
    private Joe joe;

    @Before public void setup() {
        this.joe = new Joe();
    }

    @Test
    public void testIsTruthy() {
        test("testIsTruthy");
        assertTrue(Joe.isTruthy(true));
        assertTrue(Joe.isTruthy(1.0));
        assertTrue(Joe.isTruthy("abc"));
        assertFalse(Joe.isTruthy(false));
        assertFalse(Joe.isTruthy(null));
    }

    @Test
    public void testIsEqual() {
        test("testIsEqual");
        assertTrue(Joe.isEqual(null, null));
        assertTrue(Joe.isEqual(1.0, 1.0));
        assertTrue(Joe.isEqual("abc", "abc"));

        assertFalse(Joe.isEqual(null,  "abc"));
        assertFalse(Joe.isEqual("abc", null));
        assertFalse(Joe.isEqual("abc", "def"));
    }

    @Test
    public void testStringify() {
        test("testStringify");
        assertEquals("null", joe.stringify(null));
        assertEquals("2", joe.stringify(2.0));
        assertEquals("2.5", joe.stringify(2.5));
        assertEquals("abc", joe.stringify("abc"));
    }

    @Test
    public void testCodify() {
        test("testCodify");
        assertEquals("2", joe.codify(2.0));
        assertEquals("2.5", joe.codify(2.5));
        assertEquals("\"abc\"", joe.codify("abc"));
    }

    @Test
    public void testRecodify() {
        test("testRecodify");
        // NOTE: the algorithm is tested in ASTPrinterTest.
        // This is a spot check.
        assertEquals("2", joe.recodify(
            new Expr.Literal(2.0)
        ));
    }
}
