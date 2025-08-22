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
    // assertStatement()

    @Test
    public void testAssert_expectedSemiColon() {
        test("testAssert_expectedSemiColon");

        var source = """
            assert 1 == 1
            println();
            """;
        check(parse(source)).eq(
            "[line 2] error at 'println', expected ';' after assertion."
        );
    }

    //-------------------------------------------------------------------------
    // block()

    @Test
    public void testBlock_expectedRightBrace() {
        test("testBlock_expectedRightBrace");

        var source = """
            {
            """;
        check(parse(source)).eq(
            "[line 1] error at end, expected '}' after block."
        );
    }

    //-------------------------------------------------------------------------
    // breakStatement()

    @Test
    public void testBreak_expectedSemiColon() {
        test("testBreak_expectedSemiColon");

        var source = """
            break
            println();
            """;
        check(parse(source)).eq(
            "[line 2] error at 'println', expected ';' after 'break'."
        );
    }

    //-------------------------------------------------------------------------
    // continueStatement()

    @Test
    public void testContinue_expectedSemiColon() {
        test("testContinue_expectedSemiColon");

        var source = """
            continue
            println();
            """;
        check(parse(source)).eq(
            "[line 2] error at 'println', expected ';' after 'continue'."
        );
    }

    //-------------------------------------------------------------------------
    // expressionStatement()

    @Test
    public void testExpression_expectedSemiColon() {
        test("testExpression_expectedSemiColon");

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
    public void testFor_expectedLeftParen() {
        test("testFor_expectedLeftParen");

        var source = """
            for var i = 1; i < 5; i = i + 1) { }
            """;
        check(parse(source))
            .eq("[line 1] error at 'var', expected '(' after 'for'.");
    }

    @Test
    public void testFor_expectedSemiAfterCondition() {
        test("testFor_expectedSemiAfterCondition");

        var source = """
            for (var i = 1; i < 5 i = i + 1) { }
            """;
        check(parse(source)).eq(
            "[line 1] error at 'i', expected ';' after loop condition."
        );
    }

    @Test
    public void testFor_expectedRightParen() {
        test("testFor_expectedRightParen");

        var source = """
            for (var i = 1; i < 5; i = i + 1 { }
            """;
        check(parse(source)).eq(
            "[line 1] error at '{', expected ')' after loop clauses."
        );
    }

    //-------------------------------------------------------------------------
    // foreachStatement()

    // TODO

    //-------------------------------------------------------------------------
    // ifStatement()

    @Test
    public void testIf_expectedLeftParen() {
        test("testIf_expectedLeftParen");

        var source = """
            if x > 0) {}
            """;
        check(parse(source)).eq(
            "[line 1] error at 'x', expected '(' after 'if'."
        );
    }

    @Test
    public void testIf_expectedRightParen() {
        test("testIf_expectedRightParen");

        var source = """
            if (x > 0 {}
            """;
        check(parse(source)).eq(
            "[line 1] error at '{', expected ')' after 'if' condition."
        );
    }

    //-------------------------------------------------------------------------
    // matchStatement

    // TODO

    //-------------------------------------------------------------------------
    // returnStatement

    @Test
    public void testReturn_expectedSemiColon() {
        test("testReturn_expectedSemiColon");

        var source = """
            return 1
            var x;
            """;
        check(parse(source)).eq(
            "[line 2] error at 'var', expected ';' after return value."
        );
    }

    //-------------------------------------------------------------------------
    // switchStatement

    @Test public void testSwitch_expectedLeftParen() {
        test("testSwitch_expectedLeftParen");

        var source = """
            switch x) {}
            """;
        check(parse(source)).eq(
            "[line 1] error at 'x', expected '(' after 'switch'."
        );
    }

    @Test public void testSwitch_expectedRightParen() {
        test("testSwitch_expectedRightParen");

        var source = """
            switch (x {}
            """;
        check(parse(source)).eq(
            "[line 1] error at '{', expected ')' after switch expression."
        );
    }

    @Test public void testSwitch_expectedLeftBrace() {
        test("testSwitch_expectedLeftBrace");

        var source = """
            switch (x) }
            """;
        check(parse(source)).eq(
            "[line 1] error at '}', expected '{' before switch body."
        );
    }

    @Test public void testSwitch_expectedArrowAfterCase() {
        test("testSwitch_expectedArrowAfterCase");

        var source = """
            switch (x) {
                case 1 {}
            }
            """;
        check(parse(source)).eq(
            "[line 2] error at '{', expected '->' after case value."
        );
    }

    @Test public void testSwitch_expectedCase() {
        test("testSwitch_expectedCase");

        var source = """
            switch (x) {
            }
            """;
        check(parse(source)).eq(
            "[line 2] error at '}', expected at least one 'case' in switch."
        );
    }

    @Test public void testSwitch_expectedArrowAfterDefault() {
        test("testSwitch_expectedArrowAfterDefault");

        var source = """
            switch (x) {
                case 1 -> {}
                default {}
            }
            """;
        check(parse(source)).eq(
            "[line 3] error at '{', expected '->' after 'default'."
        );
    }

    @Test public void testSwitch_expectedRightBrace() {
        test("testSwitch_expectedRightBrace");

        var source = """
            switch (x) {
                case 1 -> {}
                default -> {}
            """;
        check(parse(source)).eq(
            "[line 3] error at end, expected '}' after switch body."
        );
    }

    //-------------------------------------------------------------------------
    // throwStatement

    @Test public void testThrow_expectedSemiColon() {
        test("testThrow_expectedSemiColon");

        var source = """
            throw "foo"
            """;
        check(parse(source)).eq(
            "[line 1] error at end, expected ';' after thrown error."
        );
    }

    //-------------------------------------------------------------------------
    // whileStatement()

    @Test
    public void testWhile_expectedLeftParen() {
        test("testWhile_expectedLeftParen");

        var source = """
            while x > 0) {}
            """;
        check(parse(source)).eq(
            "[line 1] error at 'x', expected '(' after 'while'."
        );
    }

    @Test
    public void testWhile_expectedRightParen() {
        test("testWhile_expectedRightParen");

        var source = """
            while (x > 0 {}
            """;
        check(parse(source)).eq(
            "[line 1] error at '{', expected ')' after condition."
        );
    }

    //-------------------------------------------------------------------------
    // assignment()

    @Test public void testAssignment_invalidTarget() {
        test("testAssignment_invalidTarget");

        var source = """
            1 = 2;
            """;
        check(parse(source)).eq(
            "[line 1] error at '=', invalid assignment target.");
    }

    //-------------------------------------------------------------------------
    // ternary()

    @Test public void testTernary_expectedColon() {
        test("testTernary_expectedColon");

        var source = """
            1 ? 2;
            """;
        check(parse(source)).eq(
            "[line 1] error at ';', expected ':' after expression.");
    }

    //-------------------------------------------------------------------------
    // prePost()

    @Test public void testPrePost_invalidTarget() {
        test("testPrePost_invalidTarget");

        var source = """
            ++1;
            """;
        check(parse(source)).eq(
            "[line 1] error at '++', invalid '++' target.");
    }

    //-------------------------------------------------------------------------
    // call()

    @Test public void testCall_expectedProperty() {
        test("testCall_expectedProperty");

        var source = """
            x.1;
            """;
        check(parse(source)).eq(
            "[line 1] error at '1', expected property name after '.'.");
    }

    //-------------------------------------------------------------------------
    // finishCall()

    @Test public void testFinishCall_expectedRightParen() {
        test("testFinishCall_expectedRightParen");

        var source = """
            foo(1, 2;
            """;
        check(parse(source)).eq(
            "[line 1] error at ';', expected ')' after arguments.");
    }

    //-------------------------------------------------------------------------
    // primary()

    @Test public void testPrimary_expectedProperty() {
        test("testPrimary_expectedProperty");

        var source = """
            .: = 5;
            """;
        check(parse(source)).eq(
            "[line 1] error at ':', expected property name.");
    }

    @Test public void testPrimary_expectedDotAfterSuper() {
        test("testPrimary_expectedDotAfterSuper");

        var source = """
            super*5;
            """;
        check(parse(source)).eq(
            "[line 1] error at '*', expected '.' after 'super'.");
    }

    @Test public void testPrimary_expectedRightParenAfterExpr() {
        test("testPrimary_expectedRightParenAfterExpr");

        var source = """
            (1 + 2;
            """;
        check(parse(source)).eq(
            "[line 1] error at ';', expected ')' after expression.");
    }

    @Test public void testPrimary_expectedExpression() {
        test("testPrimary_expectedExpression");

        var source = """
            =;
            """;
        check(parse(source)).eq(
            "[line 1] error at '=', expected expression.");
    }

    //-------------------------------------------------------------------------
    // listLiteral()

    @Test public void testListLiteral_expectedRightBracket() {
        test("testListLiteral_expectedRightBracket");

        var source = """
            [1, 2;
            """;
        check(parse(source)).eq(
            "[line 1] error at ';', expected ']' after list items.");
    }

    //-------------------------------------------------------------------------
    // setOrMapLiteral()

    @Test public void testSetOrMapLiteral_expectedRightBrace() {
        test("testSetOrMapLiteral_expectedRightBrace");

        var source = """
            var map = {:;
            """;
        check(parse(source)).eq(
            "[line 1] error at ';', expected '}' after ':' in empty map literal.");
    }

    //-------------------------------------------------------------------------
    // setLiteral()

    @Test public void testSetLiteral_expectedRightBrace() {
        test("testSetLiteral_expectedRightBrace");

        var source = """
            var set = {1, 2;
            """;
        check(parse(source)).eq(
            "[line 1] error at ';', expected '}' after set items.");
    }

    //-------------------------------------------------------------------------
    // mapLiteral()

    @Test public void testMapLiteral_expectedRightBrace() {
        test("testMapLiteral_expectedRightBrace");

        var source = """
            var map = {#a: 1, #b: 2;
            """;
        check(parse(source)).eq(
            "[line 1] error at ';', expected '}' after map items.");
    }

    //-------------------------------------------------------------------------
    // rulesetExpression()

    @Test public void testRuleSet_expectedRightBrace() {
        test("testRuleSet_expectedRightBrace");

        var source = """
            var rules = ruleset A(1); }
            """;
        check(parse(source)).eq(
            "[line 1] error at 'A', expected '{' after 'ruleset'.");
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
