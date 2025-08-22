package com.wjduquette.joe.parser;

import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.Ted;
import com.wjduquette.joe.Trace;
import org.junit.Test;

import java.util.ArrayList;

import static com.wjduquette.joe.checker.Checker.check;

/**
 * Joe's parsers are mostly tested by the scripted test suite.
 * This test suite is aimed at testing parse errors in the main Joe Parser,
 * which the scripted test suite cannot easily do.
 *
 * <p>The structure of this test suite matches the structure of the
 * Parser itself.  There is a test case for each specific parse error,
 * organized by the PatternParser method that detects the error, reading
 * from top to bottom.
 */
public class ParserTest extends Ted {
    //-------------------------------------------------------------------------
    // classDeclaration()

    @Test
    public void testClass_expectedClassName() {
        test("testClass_expectedClassName");

        var source = """
            class 123 {}
            """;
        check(parse(source))
            .eq("[line 1] error at '123', expected class name.");
    }

    @Test
    public void testClass_expectedSuperclass() {
        test("testClass_expectedSuperclass");

        var source = """
            class Thing extends 123 {}
            """;
        check(parse(source)).eq(
            "[line 1] error at '123', expected superclass name."
        );
    }

    @Test
    public void testClass_expectedLeftBrace() {
        test("testClass_expectedLeftBrace");

        var source = """
            class Thing
                method init() {}
            }
            """;
        check(parse(source)).eq(
            "[line 2] error at 'method', expected '{' before class body."
        );
    }

    @Test
    public void testClass_expectedMethod() {
        test("testClass_expectedMethod");

        var source = """
            class Thing {
                function init() {}
            }
            """;
        check(parse(source)).eq(
            "[line 2] error at 'function', expected method, static method," +
                " or static initializer."
        );
    }

    @Test
    public void testClass_afterStatic() {
        test("testClass_afterStatic");

        var source = """
            class Thing {
                static function func() {}
            }
            """;
        check(parse(source)).eq(
            "[line 2] error at 'function', expected 'method' or '{' after 'static'.");
    }

    @Test
    public void testClass_expectedRightBrace() {
        test("testClass_expectedRightBrace");

        var source = """
            class Thing {
                method init() {}
            """;
        check(parse(source)).eq(
            "[line 2] error at end, expected '}' after class body."
        );
    }

    //-------------------------------------------------------------------------
    // functionDeclaration()

    @Test
    public void testFunction_expectedName() {
        test("testFunction_expectedName");

        var source = """
            function 123() {}
            """;
        check(parse(source)).eq(
            "[line 1] error at '123', expected function name."
        );
    }

    @Test
    public void testFunction_expectedLeftParen() {
        test("testFunction_expectedLeftParen");

        var source = """
            function square x) {}
            """;
        check(parse(source)).eq(
            "[line 1] error at 'x', expected '(' after function name."
        );
    }

    @Test
    public void testFunction_expectedLeftBrace() {
        test("testFunction_expectedLeftBrace");

        var source = """
            function compute(x,y) }
            """;
        check(parse(source)).eq(
            "[line 1] error at '}', expected '{' before function body."
        );
    }

    //-------------------------------------------------------------------------
    // parameters()

    @Test
    public void testParameters_expectedParam() {
        test("testParameters_expectedParam");

        var source = """
            function compute(x,123) {}
            """;
        check(parse(source)).eq(
            "[line 1] error at '123', expected parameter name."
        );
    }

    @Test
    public void testParameters_duplicateParam() {
        test("testParameters_duplicateParam");

        var source = """
            function bad(x,y,x) {}
            """;
        check(parse(source)).eq(
            "[line 1] error at 'x', duplicate parameter name."
        );
    }

    @Test public void testParameters_recordNoArgs() {
        test("testParameters_recordNoArgs");

        var source = """
            record Thing(id, args) {}
            """;
        check(parse(source)).eq(
            "[line 1] error at 'args', record type cannot have a variable-length argument list."
        );
    }

    @Test
    public void testParameters_argsPosition() {
        test("testParameters_argsPosition");

        var source = """
            function bad(x,args,y) {}
            """;
        check(parse(source)).eq(
            "[line 1] error at 'args', 'args' must be the final parameter when present."
        );
    }

    @Test
    public void testParameters_expectedRightParen() {
        test("testParameters_expectedRightParen");

        var source = """
            function compute(x,y {}
            """;
        check(parse(source)).eq(
            "[line 1] error at '{', expected ')' after parameter list."
        );
    }

    //-------------------------------------------------------------------------
    // recordDeclaration()

    @Test
    public void testRecord_expectedRecordName() {
        test("testRecord_expectedRecordName");

        var source = """
            record 123(x) {}
            """;
        check(parse(source))
            .eq("[line 1] error at '123', expected record type name.");
    }

    @Test
    public void testRecord_expectedLeftParen() {
        test("testRecord_expectedLeftParen");

        var source = """
            record Thing {}
            """;
        check(parse(source)).eq(
            "[line 1] error at '{', expected '(' after record type name."
        );
    }

    @Test
    public void testRecord_expectedParam() {
        test("testRecord_expectedParam");

        var source = """
            record Thing() {}
            """;
        check(parse(source)).eq(
            "[line 1] error at ')', expected at least one record parameter."
        );
    }

    @Test
    public void testRecord_expectedLeftBrace() {
        test("testRecord_expectedLeftBrace");

        var source = """
            record Thing(x)
                method func() {}
            }
            """;
        check(parse(source)).eq(
            "[line 2] error at 'method', expected '{' before type body."
        );
    }

    @Test
    public void testRecord_expectedMethod() {
        test("testRecord_expectedMethod");

        var source = """
            record Thing(x) {
                function func() {}
            }
            """;
        check(parse(source)).eq(
            "[line 2] error at 'function', expected method, static method," +
                " or static initializer."
        );
    }

    @Test
    public void testRecord_afterStatic() {
        test("testRecord_afterStatic");

        var source = """
            record Thing(x) {
                static function func() {}
            }
            """;
        check(parse(source)).eq(
            "[line 2] error at 'function', expected 'method' or '{' after 'static'.");
    }

    @Test
    public void testRecord_expectedRightBrace() {
        test("testRecord_expectedRightBrace");

        var source = """
            record Thing(x) {
                method func() {}
            """;
        check(parse(source)).eq(
            "[line 2] error at end, expected '}' after type body."
        );
    }

    //-------------------------------------------------------------------------
    // varDeclaration()

    @Test
    public void testVar_expectedSemiColon() {
        test("testVar_expectedSemiColon");

        var source = """
            var x = 5
            println(x);
            """;
        check(parse(source)).eq(
            "[line 2] error at 'println', expected ';' after variable declaration."
        );
    }

    @Test
    public void testVar_expectedVarName_constant() {
        test("testVar_expectedVarName_constant");

        var source = """
            var 123 = 5;
            """;
        check(parse(source)).eq(
            "[line 1] error at '123', expected variable name."
        );
    }

    @Test
    public void testVar_expectedVarName_expr() {
        test("testVar_expectedVarName_expr");

        var source = """
            var $(x) = 5;
            """;
        check(parse(source)).eq(
            "[line 1] error at '$', expected variable name."
        );
    }

    @Test
    public void testVar_expectedVar() {
        test("testVar_expectedVar");

        var source = """
            var [1] = 5;
            """;
        check(parse(source)).eq(
            "[line 1] error at ']', 'var' pattern must declare at least one variable."
        );
    }

    @Test
    public void testVar_expectedEquals() {
        test("testVar_expectedEquals");

        var source = """
            var [x] := [5];
            """;
        check(parse(source)).eq(
            "[line 1] error at ':', expected '=' after pattern."
        );
    }

    @Test
    public void testVar_expectedSemicolon() {
        test("testVar_expectedSemicolon");

        var source = """
            var [x] = [5]
            """;
        check(parse(source)).eq(
            "[line 1] error at end, expected ';' after target expression."
        );
    }

    //-------------------------------------------------------------------------
    // OLD TESTS

    //-------------------------------------------------------------------------
    // Stmt.Assert

    @Test
    public void testStmtAssert_noSemiColon() {
        test("testStmtAssert_noSemiColon");

        var source = """
            assert 1 == 1
            println();
            """;
        check(parse(source)).eq(
            "[line 2] error at 'println', expected ';' after assertion."
        );
    }

    //-------------------------------------------------------------------------
    // Stmt.Block

    @Test
    public void testStmtBlock_noRightBrace() {
        test("testStmtBlock_noRightBrace");

        var source = """
            {
            """;
        check(parse(source)).eq(
            "[line 1] error at end, expected '}' after block."
        );
    }

    //-------------------------------------------------------------------------
    // Stmt.Class


    //-------------------------------------------------------------------------
    // Stmt.Expression

    @Test
    public void testStmtExpression_noSemiColon() {
        test("testStmtExpression_noSemiColon");

        var source = """
            1 + 2
            println();
            """;
        check(parse(source)).eq(
            "[line 2] error at 'println', expected ';' after expression."
        );
    }

    //-------------------------------------------------------------------------
    // Stmt.For

    @Test
    public void testStmtFor_noLeftParen() {
        test("testStmtFor_noLeftParen");

        var source = """
            for var i = 1; i < 5; i = i + 1) { }
            """;
        check(parse(source))
            .eq("[line 1] error at 'var', expected '(' after 'for'.");
    }

    @Test
    public void testStmtFor_noSemiAfterCondition() {
        test("testStmtFor_noSemiAfterCondition");

        var source = """
            for (var i = 1; i < 5 i = i + 1) { }
            """;
        check(parse(source)).eq(
            "[line 1] error at 'i', expected ';' after loop condition."
        );
    }

    @Test
    public void testStmtFor_noRightParen() {
        test("testStmtFor_noRightParen");

        var source = """
            for (var i = 1; i < 5; i = i + 1 { }
            """;
        check(parse(source)).eq(
            "[line 1] error at '{', expected ')' after loop clauses."
        );
    }


    //-------------------------------------------------------------------------
    // Stmt.If

    @Test
    public void testStmtIf_noLeftParen() {
        test("testStmtIf_noLeftParen");

        var source = """
            if x > 0) {}
            """;
        check(parse(source)).eq(
            "[line 1] error at 'x', expected '(' after 'if'."
        );
    }

    @Test
    public void testStmtIf_noRightParen() {
        test("testStmtIf_noRightParen");

        var source = """
            if (x > 0 {}
            """;
        check(parse(source)).eq(
            "[line 1] error at '{', expected ')' after if condition."
        );
    }

    //-------------------------------------------------------------------------
    // Stmt.Return

    @Test
    public void testStmtReturn_noSemiColon() {
        test("testStmtReturn_noSemiColon");

        var source = """
            return 1
            var x;
            """;
        check(parse(source)).eq(
            "[line 2] error at 'var', expected ';' after return value."
        );
    }

    //-------------------------------------------------------------------------
    // Stmt.Var


    //-------------------------------------------------------------------------
    // Stmt.While

    @Test
    public void testStmtWhile_noLeftParen() {
        test("testStmtWhile_noLeftParen");

        var source = """
            while x > 0) {}
            """;
        check(parse(source)).eq(
            "[line 1] error at 'x', expected '(' after 'while'."
        );
    }

    @Test
    public void testStmtWhile_noRightParen() {
        test("testStmtWhile_noRightParen");

        var source = """
            while (x > 0 {}
            """;
        check(parse(source)).eq(
            "[line 1] error at '{', expected ')' after condition."
        );
    }


    //-------------------------------------------------------------------------
    // Helpers

    private String parse(String input) {
        var errors = new ArrayList<Trace>();
        var buffer = new SourceBuffer("-", input);
        var parser = new Parser(buffer, (msg, flag) -> errors.add(msg));
        parser.parseJoe();
        check(errors.isEmpty()).eq(false);

        var trace = errors.get(0);
        return "[line " + trace.line() + "] " + trace.message();
    }
}
