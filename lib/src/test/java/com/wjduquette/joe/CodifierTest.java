package com.wjduquette.joe;

import org.junit.Before;
import org.junit.Test;
import static com.wjduquette.joe.TokenType.*;
import static com.wjduquette.joe.Expr.*;
import static com.wjduquette.joe.checker.Checker.check;

public class CodifierTest extends Ted {
    Joe joe;
    Literal one = new Literal(1.0);
    Literal two = new Literal(2.0);
    Literal decimal = new Literal(2.5);
    Literal string  = new Literal("abc");
    Variable variable  = new Variable(new Token(IDENTIFIER, "counter", null, 1));

    @Before
    public void setup() {
        joe = new Joe();
    }

    //
    // NOTE: Joe::recodify() just calls Codifier.codify().
    //

    //-------------------------------------------------------------------------
    // Expressions

    @Test
    public void testExprAssign() {
        test("testExprAssign");
        var expr = new Assign(
            new Token(IDENTIFIER, "x", null, 1),
            two
        );
        check(joe.recodify(expr)).eq("x = 2");
    }

    @Test
    public void testExprBinary_star() {
        test("testExprBinary_star");
        var bin = new Binary(
            one,
            new Token(STAR, "*", null, 1),
            two
        );
        check(joe.recodify(bin)).eq("1*2");
    }

    @Test
    public void testExprBinary_slash() {
        test("testExprBinary_slash");
        var bin = new Binary(
            one,
            new Token(SLASH, "/", null, 1),
            two
        );
        check(joe.recodify(bin)).eq("1/2");
    }

    @Test
    public void testExprBinary_other() {
        test("testExprBinary_other");
        var bin = new Binary(
            one,
            new Token(PLUS, "+", null, 1),
            two
        );
        check(joe.recodify(bin)).eq("1 + 2");
    }

    @Test
    public void testExprGrouping() {
        test("testExprGrouping");
        var group = new Grouping(decimal);
        check(joe.recodify(group)).eq("(2.5)");
    }

    @Test
    public void testExprLiteral() {
        test("testExprLiteral");
        check(joe.recodify(two)).eq("2");
        check(joe.recodify(decimal)).eq("2.5");
        check(joe.recodify(string)).eq("\"abc\"");
    }

    @Test
    public void testExprUnary() {
        test("testExprUnary");
        var unary = new Unary(
            new Token(MINUS, "-", null, 1),
            two
        );
        check(joe.recodify(unary)).eq("-2");
    }

    @Test
    public void testExprVariable() {
        test("testExprVariable");
        check(joe.recodify(variable)).eq("counter");
    }

    //-------------------------------------------------------------------------
    // Statements

    @Test
    public void testStmtExpr() {
        test("testStmtExpr");

        var stmt = new Stmt.Expression(two);
        check(joe.recodify(stmt)).eq("2;");
    }

    @Test
    public void testStmtVar() {
        test("testStmtVar");

        var stmt = new Stmt.Var(
            new Token(IDENTIFIER, "x", null, 1),
            two
        );
        check(joe.recodify(stmt)).eq("var x = 2;");
    }
}
