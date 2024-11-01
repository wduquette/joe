package com.wjduquette.joe;

import org.junit.Before;
import org.junit.Test;
import static com.wjduquette.joe.TokenType.*;
import static com.wjduquette.joe.Expr.*;
import static com.wjduquette.joe.checker.Checker.check;

public class CodifierTest extends Ted {
    Joe joe;
    Codifier codifier;
    Literal one = new Literal(1.0);
    Literal two = new Literal(2.0);
    Literal decimal = new Literal(2.5);
    Literal string  = new Literal("abc");
    Variable variable  = new Variable(new Token(IDENTIFIER, "counter", null, 1));

    @Before
    public void setup() {
        joe = new Joe();
        codifier = new Codifier(joe);
    }

    //
    // NOTE: Joe::recodify() just calls Codifier.codify().
    //

    //-------------------------------------------------------------------------
    // Expressions

    @Test
    public void testExprBinary_star() {
        test("testExprBinary_star");
        var bin = new Binary(
            one,
            new Token(STAR, "*", null, 1),
            two
        );
        check(codifier.recodify(bin)).eq("1*2");
    }

    @Test
    public void testExprBinary_slash() {
        test("testExprBinary_slash");
        var bin = new Binary(
            one,
            new Token(SLASH, "/", null, 1),
            two
        );
        check(codifier.recodify(bin)).eq("1/2");
    }

    @Test
    public void testExprBinary_other() {
        test("testExprBinary_other");
        var bin = new Binary(
            one,
            new Token(PLUS, "+", null, 1),
            two
        );
        check(codifier.recodify(bin)).eq("1 + 2");
    }

    @Test
    public void testExprGrouping() {
        test("testExprGrouping");
        var group = new Grouping(decimal);
        check(codifier.recodify(group)).eq("(2.5)");
    }

    @Test
    public void testExprLiteral() {
        test("testExprLiteral");
        check(codifier.recodify(two)).eq("2");
        check(codifier.recodify(decimal)).eq("2.5");
        check(codifier.recodify(string)).eq("\"abc\"");
    }

    @Test
    public void testExprUnary() {
        test("testExprUnary");
        var unary = new Unary(
            new Token(MINUS, "-", null, 1),
            two
        );
        check(codifier.recodify(unary)).eq("-2");
    }

    @Test
    public void testExprVariable() {
        test("testExprVariable");
        check(codifier.recodify(variable)).eq("counter");
    }
}
