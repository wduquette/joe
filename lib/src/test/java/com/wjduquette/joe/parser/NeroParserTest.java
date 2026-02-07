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
 * This test suite is aimed at testing parse errors in the Nero parser,
 * which the scripted test suite cannot easily do.
 *
 * <p>The structure of this test suite matches the structure of the
 * `NeroParser` itself.  There is a test case for each specific parse error,
 * organized by the `NeroParser` method that detects the error, reading
 * from top to bottom.
 */
public class NeroParserTest extends Ted {
    private final List<Trace> errors = new ArrayList<>();

    //-------------------------------------------------------------------------
    // parse()

    @Test public void testParse_axiomBuiltIn() {
        test("testParse_axiomBuiltIn");

        var source = """
            member(#joe, 90);
            """;
        check(parseNero(source))
            .eq("[line 1] error at 'member', found built-in predicate in axiom.");
    }

    @Test public void testParse_axiomUndefined() {
        test("testParse_axiomUndefined");

        var source = """
            Person(#joe);
            """;
        check(parseNero(source))
            .eq("[line 1] error at 'Person', undefined relation in axiom.");
    }

    @Test public void testParse_axiomMismatch() {
        test("testParse_axiomMismatch");

        var source = """
            define Person/id;
            Person(#joe, 90);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'Person', schema mismatch, expected shape compatible with 'Person/id', got: 'Person(#joe, 90)'.");
    }

    @Test public void testParse_headBuiltIn() {
        test("testParse_headBuiltIn");

        var source = """
            member(x, y) :- Foo(x, y);
            """;
        check(parseNero(source))
            .eq("[line 1] error at 'member', found built-in predicate in rule head.");
    }

    @Test public void testParse_headUndefined() {
        test("testParse_headUndefined");

        var source = """
            Person(x) :- Someone(x);
            """;
        check(parseNero(source))
            .eq("[line 1] error at 'Person', undefined relation in rule head.");
    }

    @Test public void testParse_headMismatch() {
        test("testParse_headMismatch");

        var source = """
            define Result/value;
            Result(x, y) :- Person(x, y);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'Result', schema mismatch, expected shape compatible with 'Result/value', got: 'Result(x, y)'.");
    }

    @Test public void testParse_expectedAxiomOrRule() {
        test("testParse_expectedAxiomOrRule");

        var source = """
            Head(x), Body(x);
            """;
        check(parseNero(source))
            .eq("[line 1] error at ',', expected declaration, axiom, or rule.");
    }

    //-------------------------------------------------------------------------
    // transientDeclaration

    @Test public void testTransientDeclaration_expectedRelation() {
        test("testTransientDeclaration_expectedRelation");

        var source = """
            transient 2;
            """;
        check(parseNero(source))
            .eq("[line 1] error at '2', expected relation after 'transient'.");
    }

    @Test public void testTransientDeclaration_foundBuiltIn() {
        test("testTransientDeclaration_foundBuiltIn");

        var source = """
            transient member;
            """;
        check(parseNero(source))
            .eq("[line 1] error at 'member', found built-in predicate in 'transient' declaration.");
    }

    @Test public void testTransientDeclaration_expectedSemicolon() {
        test("testDefineDeclaration_expectedSemicolon");

        var source = """
            transient Person:
            """;
        check(parseNero(source))
            .eq("[line 1] error at ':', expected ';' after relation.");
    }

    //-------------------------------------------------------------------------
    // defineDeclaration

    @Test public void testDefineDeclaration_expectedRelation() {
        test("testDefineDeclaration_expectedRelation");

        var source = """
            define 2;
            """;
        check(parseNero(source))
            .eq("[line 1] error at '2', expected relation after 'define [transient]'.");
    }

    @Test public void testDefineDeclaration_foundBuiltIn() {
        test("testDefineDeclaration_foundBuiltIn");

        var source = """
            define member/2;
            """;
        check(parseNero(source))
            .eq("[line 1] error at 'member', found built-in predicate in 'define' declaration.");
    }

    @Test public void testDefineDeclaration_expectedSlash() {
        test("testDefineDeclaration_expectedSlash");

        var source = """
            define Person:2;
            """;
        check(parseNero(source))
            .eq("[line 1] error at ':', expected '/' after relation.");
    }

    @Test public void testDefineDeclaration_expectedFieldName() {
        test("testDefineDeclaration_expectedFieldName");

        var source = """
            define Thing/id, 2;
            """;
        check(parseNero(source))
            .eq("[line 1] error at '2', expected field name.");
    }

    @Test public void testDefineDeclaration_duplicateFieldName() {
        test("testDefineDeclaration_duplicateFieldName");

        var source = """
            define Thing/id, id;
            """;
        check(parseNero(source))
            .eq("[line 1] error at 'id', duplicate field name.");
    }

    @Test public void testDefineDeclaration_expectedValidShape() {
        test("testDefineDeclaration_expectedValidShape");

        var source = """
            define Thing/null;
            """;
        check(parseNero(source))
            .eq("[line 1] error at 'null', expected arity, '...', or field names.");
    }

    @Test public void testDefineDeclaration_expectedSemicolon() {
        test("testDefineDeclaration_expectedSemicolon");

        var source = """
            define Thing/x
            define Place/x;
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'define', expected ';' after definition.");
    }

    @Test public void testDefineDeclaration_definitionClashes() {
        test("testDefineDeclaration_definitionClashes");

        var source = """
            define Thing/a,b;
            Thing(#a, #b);
            define Thing/a;
            """;
        check(parseNero(source))
            .eq("[line 3] error at 'Thing', definition clashes with earlier entry.");
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
    // axiom()

    @Test public void testAxiom_aggregate() {
        test("testAxiom_aggregate");

        var source = """
            define Thing/a;
            Thing(sum(x));
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'Thing', found aggregation function in axiom.");
    }

    @Test public void testAxiom_variable() {
        test("testAxiom_variable");

        var source = """
            define Thing/a;
            Thing(x);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'Thing', found variable in axiom.");
    }

    //-------------------------------------------------------------------------
    // rule()

    @Test public void testRule_foundUpdateMarker() {
        test("testRule_foundUpdateMarker");

        var source = """
            define Thing/a;
            Thing(x) :- Bar!(x);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'Bar', found update marker '!' in body atom of non-updating rule.");
    }

    @Test public void testRule_defaultInBuiltIn() {
        test("testRule_defaultInBuiltIn");

        var source = """
            define Thing/a;
            Thing(x) :- mapsTo(#f,#a, x | null);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'mapsTo', found variable with default value in built-in predicate.");
    }

    @Test public void testRule_defaultInNegated() {
        test("testRule_defaultInNegated");

        var source = """
            define Thing/a;
            Thing(x) :- not A(x | null);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'A', found variable with default value in negated atom.");
    }

    @Test public void testRule_defaultInUnbound() {
        test("testRule_defaultUnbound");

        var source = """
            define Thing/a;
            Thing(x) :- A(x | y);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'A', default value has unbound variable: 'y'.");
    }

    @Test public void testRule_negatedUnbound() {
        test("testRule_negatedUnbound");

        var source = """
            define Thing/a;
            Thing(x) :- not Attribute(y);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'Attribute', negated atom contains unbound variable: 'y'.");
    }

    @Test public void testRule_duplicateDefaults() {
        test("testRule_duplicateDefaults");

        var source = """
            define Thing/a;
            Thing(x) :- A(x | null), B(x | null);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'B', variable has multiple default values: 'x'.");
    }

    @Test public void testRule_referencedDefault() {
        test("testRule_duplicateDefaults");

        var source = """
            define Thing/a;
            Thing(x) :- A(x), B(x | null);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'A', defaulted variable is referenced in body atom(s): 'x'.");
    }


    @Test public void testRule_headUnbound() {
        test("testRule_headUnbound");

        var source = """
            define Thing/a;
            Thing(x) :- Attribute(y);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'Thing', found unbound variable(s) in rule head.");
    }

    @Test public void testRule_expectedSemicolon() {
        test("testRule_expectedSemicolon");

        var source = """
            define A/x;
            define C/x;
            A(x) :- B(x)
            C(#x);
            """;
        check(parseNero(source))
            .eq("[line 4] error at 'C', expected ';' after rule body.");
    }

    @Test public void testRule_headWildcard() {
        test("testRule_headWildcard");

        var source = """
            define Thing/a;
            Thing(_) :- Attribute(y);
            """;
        check(parseNero(source))
            .eq("[line 2] error at '_', found wildcard in axiom or head atom.");
    }

    //-------------------------------------------------------------------------
    // checkAggregates()

    @Test public void testCheckAggregates_count() {
        test("testCheckAggregates_count");
        var source = """
            define A/x,y;
            A(sum(x), sum(y)) :- B(x, y);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'A', rule head contains more than one aggregation function.");
    }

    @Test public void testCheckAggregates_duplicates() {
        test("testCheckAggregates_duplicates");
        var source = """
            define A/x,y;
            A(x, sum(x)) :- B(x);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'A', aggregated variable(s) found elsewhere in rule head.");
    }

    //-------------------------------------------------------------------------
    // checkBuiltIn()

    @Test public void testCheckBuiltIn_shape() {
        test("testCheckBuiltIn_shape");
        var source = """
            define Thing/x;
            Thing(x) :- Foo(list), member(x, list, y);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'member', expected member/item,collection, got: 'member(x, list, y)'.");
    }

    @Test public void testCheckBuiltIn_memberUnbound() {
        test("testCheckBuiltIn_memberUnbound");
        var source = """
            define Thing/x;
            Thing(x) :- Foo(list), member(x, items);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'member', expected bound variable or constant for term 'collection', got: 'items'.");
    }

    @Test public void testCheckBuiltIn_indexedMemberUnbound() {
        test("testCheckBuiltIn_indexedMemberUnbound");
        var source = """
            define Thing/x;
            Thing(x) :- Foo(list), indexedMember(i, x, items);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'indexedMember', expected bound variable or constant for term 'list', got: 'items'.");
    }

    @Test public void testCheckBuiltIn_keyedMemberUnbound() {
        test("testCheckBuiltIn_keyedMemberUnbound");
        var source = """
            define Thing/x;
            Thing(x) :- Foo(map), keyedMember(k, v, items);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'keyedMember', expected bound variable or constant for term 'map', got: 'items'.");
    }

    @Test public void testCheckBuiltIn_mapsTo_unknownF() {
        test("testCheckBuiltIn_mapsTo_unknownF");
        var source = """
            define Thing/x;
            Thing(n) :- Foo(s), mapsTo(y, s, n);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'mapsTo', expected bound variable or constant for term 'f', got: 'y'.");
    }

    @Test public void testCheckBuiltIn_mapsTo_unknownA() {
        test("testCheckBuiltIn_mapsTo_unknownA");
        var source = """
            define Thing/x;
            Thing(n) :- Foo(s), mapsTo(#str2num, a, n);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'mapsTo', expected bound variable or constant for term 'a', got: 'a'.");
    }

    //-------------------------------------------------------------------------
    // constraint()

    @Test public void testConstraint_expectedBound1() {
        test("testConstraint_expectedBound1");

        var source = """
            define Thing/x;
            Thing(x) :- Attribute(x) where 1 == x;
            """;
        check(parseNero(source))
            .eq("[line 2] error at '1', expected bound variable.");
    }

    @Test public void testConstraint_expectedBound2() {
        test("testConstraint_expectedBound2");

        var source = """
            define Thing/x;
            Thing(x) :- Attribute(x) where y == x;
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'y', expected bound variable.");
    }

    @Test public void testConstraint_wildcard_a() {
        test("testConstraint_wildcard_a");

        var source = """
            define Thing/x;
            Thing(x) :- Attribute(x) where _ == x;
            """;
        check(parseNero(source))
            .eq("[line 2] error at '_', found wildcard in constraint.");
    }

    @Test public void testConstraint_expectedComparison() {
        test("testConstraint_expectedComparison");

        var source = """
            define Thing/x;
            Thing(x) :- Attribute(x) where x = 1;
            """;
        check(parseNero(source))
            .eq("[line 2] error at '=', expected comparison operator.");
    }

    @Test public void testConstraint_expectedBoundOrConstant1() {
        test("testConstraint_expectedBoundOrConstant1");

        var source = """
            define Thing/x;
            Thing(x) :- Attribute(x) where x == y;
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'y', expected bound variable or constant.");
    }

    @Test public void testConstraint_expectedBoundOrConstant2() {
        test("testConstraint_expectedBoundOrConstant2");

        var source = """
            define Thing/x;
            Thing(x) :- Attribute(x) where x == _;
            """;
        check(parseNero(source))
            .eq("[line 2] error at '_', found wildcard in constraint.");
    }

    //-------------------------------------------------------------------------
    // atom()

    @Test public void testAtom_expectedRelation() {
        test("testAtom_expectedRelation");

        var source = """
            define A/x;
            A(x) :- ;
            """;
        check(parseNero(source))
            .eq("[line 2] error at ';', expected relation.");
    }

    @Test public void testAtom_expectedLeftParen() {
        test("testBodyAtom_expectedLeftParen");

        var source = """
            define A/x;
            A(X) :- B x);
            """;
        check(parseNero(source))
            .eq("[line 2] error at 'x', expected '(' after relation.");
    }

    //-------------------------------------------------------------------------
    // orderedAtom()

    @Test public void testListAtom_expectedRightParen() {
        test("testOrderedAtom_expectedRightParen");

        var source = """
            define A/x;
            A(x) :- B(x;
            """;
        check(parseNero(source))
            .eq("[line 2] error at ';', expected ')' after terms.");
    }

    //-------------------------------------------------------------------------
    // namedAtom()

    @Test public void testMapAtom_expectedFieldName() {
        test("testNamedAtom_expectedFieldName");

        var source = """
            define A/x,y;
            A(x, y) :- B(f0: x, #a);
            """;
        check(parseNero(source))
            .eq("[line 2] error at '#a', expected field name.");
    }

    @Test public void testMapAtom_expectedColon() {
        test("testNamedAtom_expectedColon");

        var source = """
            define A/x,y;
            A(x, y) :- B(f0: x, f1 #a);
            """;
        check(parseNero(source))
            .eq("[line 2] error at '#a', expected ':' after field name.");
    }


    @Test public void testMapAtom_expectedRightParen() {
        test("testNamedAtom_expectedRightParen");

        var source = """
            define A/x;
            A(x) :- B(f0: x;
            """;
        check(parseNero(source))
            .eq("[line 2] error at ';', expected ')' after terms.");
    }

    //-------------------------------------------------------------------------
    // term()

    @Test public void testTerm_defaultValueInHead() {
        test("testTerm_defaultValueInHead");

        var source = """
            A(a | 5);
            """;
        check(parseNero(source))
            .eq("[line 1] error at '|', found default variable syntax in axiom or head atom.");
    }

    @Test public void testTerm_defaultValueInConstraint() {
        test("testTerm_defaultValueInConstraint");

        var source = """
            define A/x;
            A(x) :- B(x,y) where x == y | null;
            """;
        check(parseNero(source))
            .eq("[line 2] error at '|', found default variable syntax in constraint.");
    }

    @Test public void testTerm_invalidDefaultValue() {
        test("testTerm_invalidDefaultValue");

        var source = """
            define A/x;
            A(x) :- B(id), C(id, x | _);
            """;
        check(parseNero(source))
            .eq("[line 2] error at '_', expected variable or constant as default value.");
    }

    @Test public void testTerm_expectedNumberAfterMinus() {
        test("testTerm_expectedNumberAfterMinus");

        var source = """
            A(-#abc);
            """;
        check(parseNero(source))
            .eq("[line 1] error at '#abc', expected number after '-'.");
    }

    @Test public void testTerm_expectedTerm() {
        test("testTerm_expectedTerm");

        var source = """
            A(*);
            """;
        check(parseNero(source))
            .eq("[line 1] error at '*', expected term.");
    }

    //-------------------------------------------------------------------------
    // aggregate()

    @Test public void testAggregate_unknown() {
        test("testAggregate_unknown");

        var source = """
            A(foo(x)) :- B(x);
            """;
        check(parseNero(source))
            .eq("[line 1] error at 'foo', unknown aggregation function.");
    }

    @Test public void testAggregate_expectedVariableName() {
        test("testAggregate_expectedVariableName");

        var source = """
            A(sum(1)) :- B(x);
            """;
        check(parseNero(source))
            .eq("[line 1] error at '1', expected aggregation variable name.");
    }

    @Test public void testAggregate_expectedRightParen() {
        test("testAggregate_expectedRightParen");

        var source = """
            A(sum(x :- B(x);
            """;
        check(parseNero(source))
            .eq("[line 1] error at ':-', expected ')' after aggregation variable name(s).");
    }

    @Test public void testAggregate_arity() {
        test("testAggregate_arity");

        var source = """
            A(sum(x,y) :- B(x);
            """;
        check(parseNero(source))
            .eq("[line 1] error at 'sum', expected 1 variable name(s).");
    }

    //-------------------------------------------------------------------------
    // listTerm

    @Test public void testListTerm_expectedRightBracket() {
        test("testListTerm_expectedRightBracket");

        var source = """
            A([1, 2);
            """;
        check(parseNero(source))
            .eq("[line 1] error at ')', expected ']' after list items.");
    }

    //-------------------------------------------------------------------------
    // patternTerm

    @Test public void testPatternTerm_foundExpression() {
        test("testPatternTerm_foundExpression");

        var source = """
            define A/x;
            A(x) :- B([x,$(y)]);
            """;
        check(parseNero(source))
            .eq("[line 2] error at '$', found interpolated expression in Nero pattern term.");
    }

    //-------------------------------------------------------------------------
    // setOrMapTerm

    @Test public void testSetOrMapTerm_expectedRightBrace() {
        test("testSetOrMapTerm_expectedRightBrace");

        var source = """
            A({:);
            """;
        check(parseNero(source))
            .eq("[line 1] error at ')', expected '}' after empty map literal.");
    }

    //-------------------------------------------------------------------------
    // setTerm

    @Test public void testSetTerm_expectedRightBrace() {
        test("testSetTerm_expectedRightBrace");

        var source = """
            A({1);
            """;
        check(parseNero(source))
            .eq("[line 1] error at ')', expected '}' after set items.");
    }

    //-------------------------------------------------------------------------
    // mapTerm

    @Test public void testMapTerm_expectedColon() {
        test("testMapTerm_expectedColon");

        var source = """
            A({#a: 1, #b, 2);
            """;
        check(parseNero(source))
            .eq("[line 1] error at ',', expected ':' after key term.");
    }

    @Test public void testMapTerm_expectedRightBrace() {
        test("testMapTerm_expectedRightBrace");

        var source = """
            A({#a: 1);
            """;
        check(parseNero(source))
            .eq("[line 1] error at ')', expected '}' after map items.");
    }

    //-------------------------------------------------------------------------
    // Helpers

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
