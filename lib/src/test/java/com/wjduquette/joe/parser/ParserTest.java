package com.wjduquette.joe.parser;

import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.Ted;
import com.wjduquette.joe.Trace;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.wjduquette.joe.checker.Checker.check;

/**
 * The Joe `Parser` is mostly tested by the scripted test suite.
 * This test suite is aimed at testing parse errors, which the scripted
 * test suite cannot easily do.
 *
 * <p>The structure of this test suite matches the structure of the
 * `Parser` itself.  There is a test case for each specific parse error,
 * organized by the `Parser` method that detects the error, reading
 * from top to bottom.
 */
public class ParserTest extends Ted {
    private final List<Trace> errors = new ArrayList<>();

    //-------------------------------------------------------------------------
    // parseNero()

    @Test public void testParseNero_expectedClause() {
        test("testParseNero");

        var source = """
            Head(x), Body(x);
            """;
        check(parseNero(source))
            .eq("[line 1] error at ',', expected fact or rule.");
    }

    //-------------------------------------------------------------------------
    // head()

    @Test public void testHead_expectedRelation() {
        test("testHead_expectedRelation");

        var source = """
            (x);
            """;
        check(parseNero(source))
            .eq("[line 1] error at '(', expected relation.");
    }

    @Test public void testHead_expectedLeftParen() {
        test("testHead_expectedLeftParen");

        var source = """
            Head x);
            """;
        check(parseNero(source))
            .eq("[line 1] error at 'x', expected '(' after relation.");
    }

    //-------------------------------------------------------------------------
    // fact()

    @Test public void testFact_nonConstantTerm() {
        test("testFact_nonConstantTerm");

        var source = """
            Thing(x);
            """;
        check(parseNero(source))
            .eq("[line 1] error at 'x', fact contains a non-constant term.");
    }

    //-------------------------------------------------------------------------
    // rule()

    @Test public void testRule_negatedUnbound() {
        test("testRule_negatedUnbound");

        var source = """
            Thing(x) :- not Attribute(y);
            """;
        check(parseNero(source))
            .eq("[line 1] error at 'y', negated body atom contains unbound variable.");
    }

    @Test public void testRule_expectedSemicolon() {
        test("testRule_expectedSemicolon");

        var source = """
            A(x) :- B(x)
            C(#x);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'C', expected ';' after rule body.");
    }

    @Test public void testRule_headUnbound() {
        test("testRule_headUnbound");

        var source = """
            Thing(x) :- Attribute(y);
            """;
        check(parseNero(source))
            .eq("[line 1] error at 'x', head atom contains unbound variable.");
    }

    @Test public void testRule_headWildcard() {
        test("testRule_headWildcard");

        var source = """
            Thing(_) :- Attribute(y);
            """;
        check(parseNero(source))
            .eq("[line 1] error at '_', head atom contains wildcard.");
    }

    //-------------------------------------------------------------------------
    // constraint()

    @Test public void testConstraint_expectedBound1() {
        test("testConstraint_expectedBound1");

        var source = """
            Thing(x) :- Attribute(x) where 1 == x;
            """;
        check(parseNero(source))
            .eq("[line 1] error at '1', expected bound variable.");
    }

    @Test public void testConstraint_expectedBound2() {
        test("testConstraint_expectedBound2");

        var source = """
            Thing(x) :- Attribute(x) where y == x;
            """;
        check(parseNero(source))
            .eq("[line 1] error at 'y', expected bound variable.");
    }

    @Test public void testConstraint_expectedBound3() {
        test("testConstraint_expectedBound2");

        var source = """
            Thing(x) :- Attribute(x) where _ == x;
            """;
        check(parseNero(source))
            .eq("[line 1] error at '_', expected bound variable.");
    }

    @Test public void testConstraint_expectedComparison() {
        test("testConstraint_expectedComparison");

        var source = """
            Thing(x) :- Attribute(x) where x = 1;
            """;
        check(parseNero(source))
            .eq("[line 1] error at '=', expected comparison operator.");
    }

    @Test public void testConstraint_expectedBoundOrConstant1() {
        test("testConstraint_expectedBoundOrConstant1");

        var source = """
            Thing(x) :- Attribute(x) where x == y;
            """;
        check(parseNero(source))
            .eq("[line 1] error at 'y', expected bound variable or constant.");
    }

    @Test public void testConstraint_expectedBoundOrConstant2() {
        test("testConstraint_expectedBoundOrConstant2");

        var source = """
            Thing(x) :- Attribute(x) where x == _;
            """;
        check(parseNero(source))
            .eq("[line 1] error at '_', expected bound variable or constant.");
    }

    //-------------------------------------------------------------------------
    // bodyAtom()

    @Test public void testAtom_expectedRelation() {
        test("testBodyAtom_expectedRelation");

        var source = """
            A(x) :- ;
            """;
        check(parseNero(source))
            .eq("[line 1] error at ';', expected relation.");
    }

    @Test public void testAtom_expectedLeftParen() {
        test("testBodyAtom_expectedLeftParen");

        var source = """
            A(X) :- B x);
            """;
        check(parseNero(source))
            .eq("[line 1] error at 'x', expected '(' after relation.");
    }

    //-------------------------------------------------------------------------
    // indexedAtom()

    @Test public void testOrderedAtom_expectedFieldName() {
        test("testIndexedAtom_expectedFieldName");

        var source = """
            A(x, y) :- B(f0: x, #a);
            """;
        check(parseNero(source))
            .eq("[line 1] error at '#a', expected field name.");
    }

    @Test public void testOrderedAtom_expectedColon() {
        test("testIndexedAtom_expectedColon");

        var source = """
            A(x, y) :- B(f0: x, f1 #a);
            """;
        check(parseNero(source))
            .eq("[line 1] error at '#a', expected ':' after field name.");
    }

    @Test public void testOrderedAtom_expectedRightParen() {
        test("testIndexedAtom_expectedRightParen");

        var source = """
            A(x) :- B(x;
            """;
        check(parseNero(source))
            .eq("[line 1] error at ';', expected ')' after terms.");
    }

    //-------------------------------------------------------------------------
    // indexedAtom()

    @Test public void testASTTerm_expectedTerm() {
        test("testASTTerm_expectedTerm");

        var source = """
            A(*);
            """;
        check(parseNero(source))
            .eq("[line 1] error at '*', expected term.");
    }

    //-------------------------------------------------------------------------
    // namedAtom()

    @Test public void testNamedAtom_expectedRightParen() {
        test("testNamedAtom_expectedRightParen");

        var source = """
            A(x) :- B(f0: x;
            """;
        check(parseNero(source))
            .eq("[line 1] error at ';', expected ')' after terms.");
    }

    //-------------------------------------------------------------------------
    // Helpers

    private String parseJoe(String input) {
        errors.clear();
        var buffer = new SourceBuffer("-", input);
        var parser = new Parser(buffer, this::errorHandler);
        parser.parseJoe();
        check(errors.isEmpty()).eq(false);

        var trace = errors.get(0);
        return "[line " + trace.line() + "] " + trace.message();
    }

    private String parseNero(String input) {
        errors.clear();
        var buffer = new SourceBuffer("-", input);
        var parser = new Parser(buffer, this::errorHandler);
        parser.parseNero();
        check(errors.isEmpty()).eq(false);

        var trace = errors.get(0);
        return "[line " + trace.line() + "] " + trace.message();
    }

    private void errorHandler(Trace trace, boolean incomplete) {
        errors.add(trace);
    }
}
