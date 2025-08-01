package com.wjduquette.joe.walker;

import com.wjduquette.joe.parser.Parser;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.Ted;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.wjduquette.joe.checker.Checker.checkList;

public class ParserTest extends Ted {
    private final List<String> details = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Statements

    //-------------------------------------------------------------------------
    // Stmt.Assert

    @Test
    public void testStmtAssert_noSemiColon() {
        test("testStmtAssert_noSemiColon");

        var details = parseJoe("""
            assert 1 == 1
            println();
            """);
        checkList(details).items(
            "[line 2] error at 'println', Expected ';' after assertion."
        );
    }

    //-------------------------------------------------------------------------
    // Stmt.Block

    @Test
    public void testStmtBlock_noRightBrace() {
        test("testStmtBlock_noRightBrace");

        var details = parseJoe("{");
        checkList(details).items(
            "[line 1] error at end, Expected '}' after block."
        );
    }

    //-------------------------------------------------------------------------
    // Stmt.Class

    @Test
    public void testStmtClass_badName() {
        test("testStmtClass_badName");

        var details = parseJoe("""
            class 123 {}
            """);
        checkList(details).items(
            "[line 1] error at '123', Expected class name."
        );
    }

    @Test
    public void testStmtClass_badExtends() {
        test("testStmtClass_badExtends");

        var details = parseJoe("""
            class Thing extends 123 {}
            """);
        checkList(details).items(
            "[line 1] error at '123', Expected superclass name."
        );
    }

    @Test
    public void testStmtClass_noLeftBrace() {
        test("testStmtClass_noLeftBrace");

        var details = parseJoe("""
            class Thing
                method init() {}
            }
            """);
        checkList(details).items(
            "[line 2] error at 'method', Expected '{' before class body."
        );
    }

    @Test
    public void testStmtClass_badMethod() {
        test("testStmtClass_badMethod");

        var details = parseJoe("""
            class Thing {
                function init() {}
            }
            """);
        checkList(details).items(
            "[line 2] error at 'function', Expected method, static method," +
            " or static initializer."
        );
    }

    @Test
    public void testStmtClass_noRightBrace() {
        test("testStmtClass_noRightBrace");

        var details = parseJoe("""
            class Thing {
                method init() {}
            """);
        checkList(details).items(
            "[line 2] error at end, Expected '}' after class body."
        );
    }

    //-------------------------------------------------------------------------
    // Stmt.Expression

    @Test
    public void testStmtExpression_noSemiColon() {
        test("testStmtExpression_noSemiColon");

        var details = parseJoe("""
            1 + 2
            println();
            """);
        checkList(details).items(
            "[line 2] error at 'println', Expected ';' after expression."
        );
    }

    //-------------------------------------------------------------------------
    // Stmt.For

    @Test
    public void testStmtFor_noLeftParen() {
        test("testStmtFor_noLeftParen");

        var details = parseJoe("""
            for var i = 1; i < 5; i = i + 1) { }
            """);
        checkList(details).items(
            "[line 1] error at 'var', Expected '(' after 'for'.",
            // Bogus; should skip if possible
            "[line 1] error at ')', Expected ';' after expression."
        );
    }

    @Test
    public void testStmtFor_noSemiAfterCondition() {
        test("testStmtFor_noSemiAfterCondition");

        var details = parseJoe("""
            for (var i = 1; i < 5 i = i + 1) { }
            """);
        checkList(details).items(
            "[line 1] error at 'i', Expected ';' after loop condition."
        );
    }

    @Test
    public void testStmtFor_noRightParen() {
        test("testStmtFor_noRightParen");

        var details = parseJoe("""
            for (var i = 1; i < 5; i = i + 1 { }
            """);
        checkList(details).items(
            "[line 1] error at '{', Expected ')' after loop clauses."
        );
    }

    //-------------------------------------------------------------------------
    // Stmt.Function

    @Test
    public void testStmtFunction_badName() {
        test("testStmtFunction_badName");

        var details = parseJoe("""
            function 123() {}
            """);
        checkList(details).items(
            "[line 1] error at '123', Expected function name."
        );
    }

    @Test
    public void testStmtFunction_noLeftParen() {
        test("testStmtFunction_noLeftParen");

        var details = parseJoe("""
            function square x) {}
            """);
        checkList(details).items(
            "[line 1] error at 'x', Expected '(' after function name."
        );
    }

    @Test
    public void testStmtFunction_badParam() {
        test("testStmtFunction_badParam");

        var details = parseJoe("""
            function compute(x,123) {}
            """);
        checkList(details).items(
            "[line 1] error at '123', Expected parameter name."
        );
    }

    @Test
    public void testStmtFunction_duplicateParam() {
        test("testStmtFunction_duplicateParam");

        var details = parseJoe("""
            function bad(x,y,x) {}
            """);
        checkList(details).items(
            "[line 1] error at 'x', Duplicate parameter name."
        );
    }

    @Test
    public void testStmtFunction_argsPosition() {
        test("testStmtFunction_argsPosition");

        var details = parseJoe("""
            function bad(x,args,y) {}
            """);
        checkList(details).items(
            "[line 1] error at 'args', 'args' must be the final parameter when present."
        );
    }

    @Test
    public void testStmtFunction_noRightParen() {
        test("testStmtFunction_noRightParen");

        var details = parseJoe("""
            function compute(x,y {}
            """);
        checkList(details).items(
            "[line 1] error at '{', Expected ')' after parameter list."
        );
    }

    @Test
    public void testStmtFunction_noLeftBrace() {
        test("testStmtFunction_noLeftBrace");

        var details = parseJoe("""
            function compute(x,y) }
            """);
        checkList(details).items(
            "[line 1] error at '}', Expected '{' before function body."
        );
    }

    //-------------------------------------------------------------------------
    // Stmt.If

    @Test
    public void testStmtIf_noLeftParen() {
        test("testStmtIf_noLeftParen");

        var details = parseJoe("""
            if x > 0) {}
            """);
        checkList(details).items(
            "[line 1] error at 'x', Expected '(' after 'if'."
        );
    }

    @Test
    public void testStmtIf_noRightParen() {
        test("testStmtIf_noRightParen");

        var details = parseJoe("""
            if (x > 0 {}
            """);
        checkList(details).items(
            "[line 1] error at '{', Expected ')' after if condition."
        );
    }

    //-------------------------------------------------------------------------
    // Stmt.Return

    @Test
    public void testStmtReturn_noSemiColon() {
        test("testStmtReturn_noSemiColon");

        var details = parseJoe("""
            return 1
            var x;
            """);
        checkList(details).items(
            "[line 2] error at 'var', Expected ';' after return value."
        );
    }

    //-------------------------------------------------------------------------
    // Stmt.Var

    @Test
    public void testStmtVar_badName() {
        test("testStmtVar_badName");

        var details = parseJoe("""
            var 123 = 5;
            """);
        checkList(details).items(
            "[line 1] error at '123', Expected variable name."
        );
    }

    @Test
    public void testStmtVar_noSemiColon() {
        test("testStmtVar_noSemiColon");

        var details = parseJoe("""
            var x = 5
            println(x);
            """);
        checkList(details).items(
            "[line 2] error at 'println', Expected ';' after variable declaration."
        );
    }

    //-------------------------------------------------------------------------
    // Stmt.While

    @Test
    public void testStmtWhile_noLeftParen() {
        test("testStmtWhile_noLeftParen");

        var details = parseJoe("""
            while x > 0) {}
            """);
        checkList(details).items(
            "[line 1] error at 'x', Expected '(' after 'while'."
        );
    }

    @Test
    public void testStmtWhile_noRightParen() {
        test("testStmtWhile_noRightParen");

        var details = parseJoe("""
            while (x > 0 {}
            """);
        checkList(details).items(
            "[line 1] error at '{', Expected ')' after condition."
        );
    }


    //-------------------------------------------------------------------------
    // Helpers

    // Scans and returns the parse errors
    private List<String> parseJoe(String input) {
        details.clear();
        var buffer = new SourceBuffer("-", input);
        var parser = new Parser(buffer, (detail, incomplete) ->
            details.add("[line " + detail.line() + "] " + detail.message())
        );
        parser.parseJoe();

        return details;
    }
}
