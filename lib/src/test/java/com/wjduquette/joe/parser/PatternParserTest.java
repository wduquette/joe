package com.wjduquette.joe.parser;

import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.Ted;
import com.wjduquette.joe.Trace;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.wjduquette.joe.checker.Checker.check;

/**
 * Joe's parsers are mostly tested by the scripted test suite.
 * This test suite is aimed at testing parse errors in the PatternParser,
 * which the scripted test suite cannot easily do.
 *
 * <p>The structure of this test suite matches the structure of the
 * PatternParser itself.  There is a test case for each specific parse error,
 * organized by the PatternParser method that detects the error, reading
 * from top to bottom.
 */
public class PatternParserTest extends Ted {
    private final List<Trace> errors = new ArrayList<>();

    //-------------------------------------------------------------------------
    // parsePattern()

    @Test public void testParsePattern_expectedPattern() {
        test("testParsePattern_expectedPattern");

        var source = """
            var x;
            x ~ *;
            """;
        check(parse(source))
            .eq("[line 2] error at '*', expected pattern.");
    }

    //-------------------------------------------------------------------------
    // constantPattern()

    @Test public void testConstantPattern_numberAfterMinus() {
        test("testConstantPattern_numberAfterMinus");

        var source = """
            var x;
            x ~ -a;
            """;
        check(parse(source))
            .eq("[line 2] error at 'a', expected number after '-'.");
    }

    @Test public void testConstantPattern_exprInNero() {
        test("testConstantPattern_exprInNero");

        var source = """
            var rules = ruleset {
                A(x) :- B(x, [$y]);
            };
            """;
        check(parse(source))
            .eq("[line 2] error at '$', found interpolated expression in Nero pattern term.");
    }

    @Test public void testConstantPattern_expectedExprAfterDollar() {
        test("testConstantPattern_expectedExprAfterDollar");

        var source = """
            var x;
            x ~ $1;
            """;
        check(parse(source))
            .eq("[line 2] error at '1', expected identifier or '(' after '$'.");
    }

    @Test public void testConstantPattern_expectedRightParenAfterExpr() {
        test("testConstantPattern_expectedRightParenAfterExpr");

        var source = """
            var x;
            x ~ $(x;
            """;
        check(parse(source))
            .eq("[line 2] error at ';', expected ')' after interpolated expression.");
    }

    //-------------------------------------------------------------------------
    // listPattern()

    @Test public void testListPattern_expectedTailVar() {
        test("testListPattern_expectedTailVar");

        var source = """
            var x;
            x ~ [1, 2 : ];
            """;
        check(parse(source))
            .eq("[line 2] error at ']', expected tail variable after ':'.");
    }

    @Test public void testListPattern_expectedRightBracket() {
        test("testListPattern_expectedRightBracket");

        var source = """
            var x;
            x ~ [1, 2;
            """;
        check(parse(source))
            .eq("[line 2] error at ';', expected ']' after list pattern items.");
    }

    //-------------------------------------------------------------------------
    // mapPattern()

    @Test public void testMapPattern_braceAfterEmpty() {
        test("testMapPattern_braceAfterEmpty");

        var source = """
            var x;
            x ~ {:;
            """;
        check(parse(source))
            .eq("[line 2] error at ';', expected '}' after empty map pattern.");
    }

    @Test public void testMapPattern_expectedColon() {
        test("testMapPattern_expectedColon");

        var source = """
            var x;
            x ~ {#a, 1};
            """;
        check(parse(source))
            .eq("[line 2] error at ',', expected ':' after map key.");
    }

    @Test public void testMapPattern_expectedRightBrace() {
        test("testMapPattern_expectedRightBrace");

        var source = """
            var x;
            x ~ {#a: 1, #b: 2;
            """;
        check(parse(source))
            .eq("[line 2] error at ';', expected '}' after map pattern items.");
    }

    //-------------------------------------------------------------------------
    // namedFieldPattern()

    @Test public void testNamedFieldPattern_expectedFieldName() {
        test("testNamedFieldPattern_expectedFieldName");

        // Note: we know we have the first identifier and colon, or we
        // wouldn't be here.
        var source = """
            var x;
            x ~ Thing(id: #wagon, #a);
            """;
        check(parse(source))
            .eq("[line 2] error at '#a', expected field name.");
    }

    @Test public void testNamedFieldPattern_expectedColon() {
        test("testNamedFieldPattern_expectedColon");

        // Note: we know we have the first identifier and colon, or we
        // wouldn't be here.
        var source = """
            var x;
            x ~ Thing(id: #wagon, color, #red);
            """;
        check(parse(source))
            .eq("[line 2] error at ',', expected ':' after field name.");
    }

    @Test public void testNamedFieldPattern_expectedRightParen() {
        test("testNamedFieldPattern_expectedRightParen");

        var source = """
            var x;
            x ~ Thing(id: #wagon;
            """;
        check(parse(source))
            .eq("[line 2] error at ';', expected ')' after field pattern.");
    }

    //-------------------------------------------------------------------------
    // orderedFieldPattern()

    @Test public void testOrderedFieldPattern_expectedRightParen() {
        test("testOrderedFieldPattern_expectedRightParen");

        var source = """
            var x;
            x ~ Thing(#wagon, #red;
            """;
        check(parse(source))
            .eq("[line 2] error at ';', expected ')' after field pattern.");
    }

    //-------------------------------------------------------------------------
    // Helpers

    private String parse(String input) {
        errors.clear();
        var buffer = new SourceBuffer("-", input);
        var parser = new Parser(buffer, this::errorHandler);
        parser.parseJoe();
        check(errors.isEmpty()).eq(false);

        var trace = errors.get(0);
        return "[line " + trace.line() + "] " + trace.message();
    }

    private void errorHandler(Trace trace, boolean incomplete) {
        errors.add(trace);
    }
}
