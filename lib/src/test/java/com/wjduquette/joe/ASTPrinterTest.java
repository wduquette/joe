package com.wjduquette.joe;

import org.junit.Before;
import org.junit.Test;
import static com.wjduquette.joe.TokenType.*;
import static com.wjduquette.joe.Expr.*;
import static com.wjduquette.joe.checker.Checker.check;
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

    //
    // NOTE: Joe::recodify() just calls ASTPrinter.codify().
    //

    @Test
    public void testLiteral() {
        test("testLiteral");
        check(joe.recodify(two)).eq("2");
        check(joe.recodify(decimal)).eq("2.5");
        check(joe.recodify(string)).eq("\"abc\"");
    }

    @Test
    public void testUnary() {
        test("testUnary");
        var unary = new Unary(
            new Token(MINUS, "-", null, 1),
            two
        );
        check(joe.recodify(unary)).eq("-2");
    }

    @Test
    public void testGrouping() {
        test("testGrouping");
        var group = new Grouping(decimal);
        check(joe.recodify(group)).eq("(2.5)");
    }

    @Test
    public void testBinary_star() {
        test("testBinary_star");
        var bin = new Binary(
            one,
            new Token(STAR, "*", null, 1),
            two
        );
        check(joe.recodify(bin)).eq("1*2");
    }

    @Test
    public void testBinary_slash() {
        test("testBinary_slash");
        var bin = new Binary(
            one,
            new Token(SLASH, "/", null, 1),
            two
        );
        check(joe.recodify(bin)).eq("1/2");
    }

    @Test
    public void testBinary_other() {
        test("testBinary_other");
        var bin = new Binary(
            one,
            new Token(PLUS, "+", null, 1),
            two
        );
        check(joe.recodify(bin)).eq("1 + 2");
    }
}
