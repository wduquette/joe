package com.wjduquette.joe;

import org.junit.Before;
import org.junit.Test;
import static com.wjduquette.joe.TokenType.*;
import static com.wjduquette.joe.Expr.*;
import static org.junit.Assert.assertEquals;

public class ASTPrinterTest extends Ted {
    Joe joe;
    Literal one = new Literal(1.0);
    Literal two = new Literal(2.0);
    Literal decimal = new Literal(2.5);
    Literal string  = new Literal("abc");

    @Before
    public void setup() {
        joe = new Joe();
    }

    @Test
    public void testLiteral() {
        test("testLiteral");
        assertEquals("2", joe.recodify(two));
        assertEquals("2.5", joe.recodify(decimal));
        assertEquals("\"abc\"", joe.recodify(string));
    }

    @Test
    public void testUnary() {
        test("testUnary");
        var unary = new Unary(
            new Token(MINUS, "-", null, 1),
            two
        );
        assertEquals("-2", joe.recodify(unary));
    }

    @Test
    public void testGrouping() {
        test("testGrouping");
        var group = new Grouping(decimal);
        assertEquals("(2.5)", joe.recodify(group));
    }

    @Test
    public void testBinary_star() {
        test("testBinary_star");
        var bin = new Binary(
            one,
            new Token(STAR, "*", null, 1),
            two
        );
        assertEquals("1*2", joe.recodify(bin));
    }

    @Test
    public void testBinary_slash() {
        test("testBinary_slash");
        var bin = new Binary(
            one,
            new Token(SLASH, "/", null, 1),
            two
        );
        assertEquals("1/2", joe.recodify(bin));
    }

    @Test
    public void testBinary_other() {
        test("testBinary_other");
        var bin = new Binary(
            one,
            new Token(PLUS, "+", null, 1),
            two
        );
        assertEquals("1 + 2", joe.recodify(bin));
    }
}
