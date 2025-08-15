package com.wjduquette.joe.nero;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.Ted;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wjduquette.joe.checker.Checker.check;
import static com.wjduquette.joe.checker.Checker.checkThrow;

// Tests for Nero semantics. Most possible errors are detectable
// at compile time and are found by the `NeroParser`; detection is tested
// by `parser.NeroParserTest`.
public class RuleEngineTest extends Ted {
    private final Nero nero = new Nero(new Joe());

    //-------------------------------------------------------------------------
    // Stratification Conditions

    // Unstratifiable due to negation.
    @Test public void testUnstratified_negation() {
        test("testUnstratified_negation");

        var source = """
            A(x) :- B(x), not C(x);
            C(x) :- A(x);
            """;
        checkThrow(() -> execute(source))
            .containsString("Nero rule set cannot be stratified.");
    }

    @Test public void testUnstratified_list_literal() {
        test("testUnstratified_list_literal");

        var source = """
            A([x]) :- B(x);
            B(x) :- A(x);
            """;
        checkThrow(() -> execute(source))
            .containsString("Nero rule set cannot be stratified.");
    }

    @Test public void testUnstratified_map_literal() {
        test("testUnstratified_map_literal");

        var source = """
            A({#id: x}) :- B(x);
            B(x) :- A(x);
            """;
        checkThrow(() -> execute(source))
            .containsString("Nero rule set cannot be stratified.");
    }

    @Test public void testUnstratified_set_literal() {
        test("testUnstratified_set_literal");

        var source = """
            A({x}) :- B(x);
            B(x) :- A(x);
            """;
        checkThrow(() -> execute(source))
            .containsString("Nero rule set cannot be stratified.");
    }

    @Test public void testUnstratified_aggregator() {
        test("testUnstratified_aggregator");

        var source = """
            A(sum(x)) :- B(x);
            B(x) :- A(x);
            """;
        checkThrow(() -> execute(source))
            .containsString("Nero rule set cannot be stratified.");
    }

    //-------------------------------------------------------------------------
    // Basic Operation

    // Verify that a simple Nero program can be read and executed.
    @Test public void testSimple() {
        test("testSimple_orderedAtoms");
        var source = """
            Parent(#walker, #bert);
            Parent(#bert, #clark);
            Ancestor(x, y) :- Parent(x, y);
            Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
            """;
        check(execute(source)).eq("""
            define Ancestor/2;
            Ancestor(#bert, #clark);
            Ancestor(#walker, #bert);
            Ancestor(#walker, #clark);
            
            define Parent/2;
            Parent(#bert, #clark);
            Parent(#walker, #bert);
            """);
    }

    // Verify that named atoms can be used as axioms, head atoms,
    // and body atoms.
    @Test public void testSimple_namedAtoms() {
        test("testSimple_named");
        var source = """
            Parent(p: #walker, c: #bert);
            Parent(p: #bert, c: #clark);
            Ancestor(a: x, d: y) :- Parent(p: x, c: y);
            Ancestor(a: x, d: y) :- Parent(p: x, c: z), Ancestor(a: z, d: y);
            """;
        check(execute(source)).eq("""
            define Ancestor/...;
            Ancestor(a: #bert, d: #clark);
            Ancestor(a: #walker, d: #bert);
            Ancestor(a: #walker, d: #clark);
            
            define Parent/...;
            Parent(c: #bert, p: #walker);
            Parent(c: #clark, p: #bert);
            """);
    }

    //-------------------------------------------------------------------------
    // Negation

    // Test that body atoms can be negated.
    @Test public void testNegation() {
        test("testNegation");
        var source = """
            Thing(#desk);
            Thing(#pen);
            Location(#desk, #office);
            Homeless(x) :- Thing(x), not Location(x, _);
            """;
        check(execute(source)).eq("""
            define Homeless/1;
            Homeless(#pen);
            
            define Location/2;
            Location(#desk, #office);
            
            define Thing/1;
            Thing(#desk);
            Thing(#pen);
            """);
    }

    //-------------------------------------------------------------------------
    // Constraints

    // Verifies constraint execution given a single constraint.
    @Test public void testConstraint_single() {
        test("testConstraint_single");
        var source = """
            Thing(#pen,     1);
            Thing(#desk,    10);
            Thing(#whatsit, #unknown);
            
            // #whatsit is neither large nor small
            Small(x) :- Thing(x, size) where size < 5;
            Large(x) :- Thing(x, size) where size > 5;
            """;
        check(execute(source)).eq("""
            define Large/1;
            Large(#desk);
            
            define Small/1;
            Small(#pen);
            
            define Thing/2;
            Thing(#desk, 10);
            Thing(#pen, 1);
            Thing(#whatsit, #unknown);
            """);
    }

    // Verifies constraint execution given multiple constraint.
    @Test public void testConstraint_double() {
        test("testConstraint_double");
        var source = """
            Thing(#pen,     1);
            Thing(#table,   10);
            Thing(#desk,    10);
            Thing(#whatsit, #unknown);
            
            // #whatsit is neither large nor small
            AsBigAs(x, y) :- Thing(x, xs), Thing(y, ys)
                where xs >= ys, x != y;
            """;
        check(execute(source)).eq("""
            define AsBigAs/2;
            AsBigAs(#desk, #pen);
            AsBigAs(#desk, #table);
            AsBigAs(#table, #desk);
            AsBigAs(#table, #pen);
            
            define Thing/2;
            Thing(#desk, 10);
            Thing(#pen, 1);
            Thing(#table, 10);
            Thing(#whatsit, #unknown);
            """);
    }

    @Test public void testConstraint_operators() {
        test("testConstraint_operators");
        var source = """
            transient A;
            A(2);
            
            transient B;
            B(1);
            B(2);
            B(3);
            
            EQ(x,y) :- A(x), B(y) where x == y;
            NE(x,y) :- A(x), B(y) where x != y;
            GT(x,y) :- A(x), B(y) where x > y;
            GE(x,y) :- A(x), B(y) where x >= y;
            LT(x,y) :- A(x), B(y) where x < y;
            LE(x,y) :- A(x), B(y) where x <= y;
            """;
        check(execute(source)).eq("""
            define EQ/2;
            EQ(2, 2);
            
            define GE/2;
            GE(2, 1);
            GE(2, 2);
            
            define GT/2;
            GT(2, 1);
            
            define LE/2;
            LE(2, 2);
            LE(2, 3);
            
            define LT/2;
            LT(2, 3);
            
            define NE/2;
            NE(2, 1);
            NE(2, 3);
            """);
    }

    //-------------------------------------------------------------------------
    // Miscellaneous Matching Details

    // Verifies that a variable is bound on first appearance in an atom,
    // and must be matched on second appearance even in the same atom.
    @Test public void testBindAndMatch() {
        test("testBindAndMatch");
        var source = """
            Pair(#a, #b);
            Pair(#c, #c);
            Twin(x) :- Pair(x, x);
            """;
        check(execute(source)).eq("""
            define Pair/2;
            Pair(#a, #b);
            Pair(#c, #c);
            
            define Twin/1;
            Twin(#c);
            """);
    }

    @Test
    public void testKeywordMatchesEnum() {
        test("testKeywordMatchesEnum");
        Set<Fact> facts = Set.of(
            new ListFact("Topic", List.of(Topic.THIS, "abc")),
            new ListFact("Topic", List.of(Topic.THAT, "def"))
        );
        var source = """
            Match(x) :- Topic(#this, x);
            """;
        check(inferRaw(source, facts)).eq("""
            ListFact[relation=Match, fields=[abc]]
            """);
    }

    //-------------------------------------------------------------------------
    // Fact Creation and `define` declarations

    // Ordered atoms produce ListFacts by default.
    @Test public void testDefine_list_implicit() {
        test("testDefine_list_implicit");
        var source = """
            Pair(#c, #c);
            Twin(x) :- Pair(x, x);
            """;
        check(execute(source)).eq("""
            define Pair/2;
            Pair(#c, #c);
            
            define Twin/1;
            Twin(#c);
            """);
    }

    // Ordered atoms produce ListFacts given the define relation/n
    @Test public void testDefine_list_explicit() {
        test("testDefine_list_explicit");
        var source = """
            define Pair/2;
            Pair(#c, #c);
            
            define Twin/1;
            Twin(x) :- Pair(x, x);
            """;
        check(execute(source)).eq("""
            define Pair/2;
            Pair(#c, #c);
            
            define Twin/1;
            Twin(#c);
            """);
    }

    // Named atoms produce MapFacts by default.
    @Test public void testDefine_map_implicit() {
        test("testDefine_map_implicit");
        var source = """
            Pair(first: #c, second: #c);
            Twin(id: x) :- Pair(first: x, second: x);
            """;
        check(execute(source)).eq("""
            define Pair/...;
            Pair(first: #c, second: #c);
            
            define Twin/...;
            Twin(id: #c);
            """);
    }


    // Named atoms produce MapFacts given define relation/...;
    @Test public void testDefine_map_explicit() {
        test("testDefine_map_explicit");
        var source = """
            define Pair/...;
            Pair(first: #c, second: #c);
            
            define Twin/...;
            Twin(id: x) :- Pair(first: x, second: x);
            """;
        check(execute(source)).eq("""
            define Pair/...;
            Pair(first: #c, second: #c);
            
            define Twin/...;
            Twin(id: #c);
            """);
    }

    // Given defines with field names, ordered atoms produce PairFacts.
    @Test public void testDefine_pair_explicit() {
        test("testPairFactCreation");
        var source = """
            define Pair/left, right;
            Pair(#c, #c);
            define Twin/id;
            Twin(x) :- Pair(x, x);
            """;
        check(execute(source)).eq("""
            define Pair/left,right;
            Pair(#c, #c);
            
            define Twin/id;
            Twin(#c);
            """);
    }

    //-------------------------------------------------------------------------
    // Transience

    // Facts inferred by axioms for transient relations are not retained.
    @Test public void testTransient_axiom() {
        test("testTransient_axiom");

        var source = """
            transient A;
            A(#a);
            B(#b);
            """;
        check(execute(source)).eq("""
            define B/1;
            B(#b);
            """);
    }

    // Facts inferred by rules for transient relations are not retained.
    @Test public void testTransient_rule() {
        test("testTransient_rule");

        var source = """
            transient B;
            A(#a);
            B(x) :- A(x);
            C(x) :- B(x);
            """;
        check(execute(source)).eq("""
            define A/1;
            A(#a);
            
            define C/1;
            C(#a);
            """);
    }

    // `define transient` also triggers transience.
    @Test public void testTransient_define() {
        test("testTransient_define");

        var source = """
            define transient A/1;
            define transient B/1;
            A(#a);
            B(x) :- A(x);
            C(x) :- B(x);
            """;
        check(execute(source)).eq("""
            define C/1;
            C(#a);
            """);
    }

    //-------------------------------------------------------------------------
    // Updating Semantics

    @Test public void testUpdating_axioms() {
        test("testUpdating_axioms");
        var source = """
            A(#a);
            A!(#b, #c);
            """;
        check(execute(source)).eq("""
            define A/2;
            A(#b, #c);
            """);
    }

    @Test public void testUpdating_rules() {
        test("testUpdating_rules");
        var source = """
            A(#a, 5);
            A(#b, 7);
            A!(x) :- A(x, _);
            """;
        check(execute(source)).eq("""
            define A/1;
            A(#a);
            A(#b);
            """);
    }

    //-------------------------------------------------------------------------
    // Built-in _predicates

    @Test public void testBuiltIn_member_disaggregate() {
        test("testBuiltIn_member_disaggregate");
        var source = """
            transient Owner;
            Owner(#joe, [#hat, #boots, #truck]);
            Owns(id, item) :- Owner(id, list), member(item, list);
            """;
        check(execute(source)).eq("""
            define Owns/2;
            Owns(#joe, #boots);
            Owns(#joe, #hat);
            Owns(#joe, #truck);
            """);
    }

    @Test public void testBuiltIn_member_match() {
        test("testBuiltIn_member_match");
        var source = """
            transient Owner;
            Owner(#joe, [#hat, #boots, #truck]);
            OwnsHat(id) :- Owner(id, list), member(#hat, list);
            """;
        check(execute(source)).eq("""
            define OwnsHat/1;
            OwnsHat(#joe);
            """);
    }

    @Test public void testBuiltIn_member_noCollection() {
        test("testBuiltIn_member_noCollection");
        var source = """
            transient Owner;
            Owner(#joe, #notCollection);
            Owns(id, item) :- Owner(id, list), member(item, list);
            """;
        check(execute(source)).eq("""
            """);
    }

    @Test public void testBuiltIn_indexedMember_disaggregate() {
        test("testBuiltIn_indexedMember_disaggregate");
        var source = """
            transient Owner;
            Owner(#joe, [#hat, #boots, #truck]);
            Owns(id, i, item) :- Owner(id, list), indexedMember(i, item, list);
            """;
        check(execute(source)).eq("""
            define Owns/3;
            Owns(#joe, 0, #hat);
            Owns(#joe, 1, #boots);
            Owns(#joe, 2, #truck);
            """);
    }

    @Test public void testBuiltIn_indexedMember_match() {
        test("testBuiltIn_indexedMember_match");
        var source = """
            transient Owner;
            Owner(#joe, [#hat, #boots, #truck]);
            OwnsHat(id, i) :- Owner(id, list), indexedMember(i, #hat, list);
            """;
        check(execute(source)).eq("""
            define OwnsHat/2;
            OwnsHat(#joe, 0);
            """);
    }

    @Test public void testBuiltIn_indexedMember_noCollection() {
        test("testBuiltIn_indexedMember_noCollection");
        var source = """
            transient Owner;
            Owner(#joe, #notCollection);
            Owns(id, i, item) :- Owner(id, list), indexedMember(i, item, list);
            """;
        check(execute(source)).eq("""
            """);
    }

    @Test public void testBuiltIn_keyedMember_disaggregate() {
        test("testBuiltIn_keyedMember_disaggregate");
        var source = """
            transient Owner;
            Owner(#joe, {#head: #hat, #feet: #boots});
            Wears(id, k, v) :- Owner(id, map), keyedMember(k, v, map);
            """;
        check(execute(source)).eq("""
            define Wears/3;
            Wears(#joe, #feet, #boots);
            Wears(#joe, #head, #hat);
            """);
    }

    @Test public void testBuiltIn_keyedMember_match() {
        test("testBuiltIn_keyedMember_match");
        var source = """
            transient Owner;
            Owner(#joe, {#head: #hat, #feet: #boots});
            WearsHat(id, k) :- Owner(id, map), keyedMember(k, #hat, map);
            """;
        check(execute(source)).eq("""
            define WearsHat/2;
            WearsHat(#joe, #head);
            """);
    }

    @Test public void testBuiltIn_keyedMember_noCollection() {
        test("testBuiltIn_keyedMember_noCollection");
        var source = """
            transient Owner;
            Owner(#joe, #notCollection);
            Wears(id, k, v) :- Owner(id, map), keyedMember(k, v, map);
            """;
        check(execute(source)).eq("""
            """);
    }

    //-------------------------------------------------------------------------
    // Collection Literals

    // Verify that list literals can be used in axioms.
    @Test public void testListLiteral_axioms() {
        test("testListLiteral_axioms");
        var source = """
            A([]);
            A([#a, 5]);
            """;
        check(execute(source)).eq("""
            define A/1;
            A([#a, 5]);
            A([]);
            """);
    }

    // Verify that list literals can be used in rules.
    @Test public void testListLiteral_rules() {
        test("testListLiteral_rules");
        var source = """
            transient A;
            A(#a, 5);
            B([x, y]) :- A(x, y);
            C(x, []) :- A(x, _);
            """;
        check(execute(source)).eq("""
            define B/1;
            B([#a, 5]);
            
            define C/2;
            C(#a, []);
            """);
    }

    // Verify that map literals can be used in axioms.
    @Test public void testMapLiteral_axioms() {
        test("testMapLiteral_axioms");
        var source = """
            A({:});
            A({#a: 5});
            """;
        check(execute(source)).eq("""
            define A/1;
            A({#a: 5});
            A({:});
            """);
    }

    // Verify that map literals can be used in rules.
    @Test public void testMapLiteral_rules() {
        test("testMapLiteral_rules");
        var source = """
            transient A;
            A(#a, 5);
            B({x: y}) :- A(x, y);
            C(x, {:}) :- A(x, _);
            """;
        check(execute(source)).eq("""
            define B/1;
            B({#a: 5});
            
            define C/2;
            C(#a, {:});
            """);
    }

    // Verify that set literals can be used in axioms.
    @Test public void testSetLiteral_axioms() {
        test("testSetLiteral_axioms");
        var source = """
            A({});
            A({#a, 5});
            """;
        check(execute(source)).eq("""
            define A/1;
            A({#a, 5});
            A({});
            """);
    }

    // Verify that set literals can be used in rules.
    @Test public void testSetLiteral_rules() {
        test("testSetLiteral_rules");
        var source = """
            transient A;
            A(#a, 5);
            B({x, y}) :- A(x, y);
            C(x, {}) :- A(x, _);
            """;
        check(execute(source)).eq("""
            define B/1;
            B({#a, 5});
            
            define C/2;
            C(#a, {});
            """);
    }

    //-------------------------------------------------------------------------
    // Aggregation Functions

    // Verify that we aggregate all matches, sorting by numeric indices.
    @Test public void testAggregate_indexedList_numbers() {
        test("testAggregate_indexedList_numbers");
        var source = """
            transient A;
            A(#a, 2);
            A(#b, 1);
            A(#c, 4);
            A(#d, 3);
            B(indexedList(i, item)) :- A(item, i);
            """;
        check(execute(source)).eq("""
            define B/1;
            B([#b, #a, #d, #c]);
            """);
    }

    // Verify that we aggregate all matches, sorting by string indices.
    @Test public void testAggregate_indexedList_strings() {
        test("testAggregate_indexedList_strings");
        var source = """
            transient A;
            A(#a, "2");
            A(#b, "1");
            A(#c, "4");
            A(#d, "3");
            B(indexedList(i, item)) :- A(item, i);
            """;
        check(execute(source)).eq("""
            define B/1;
            B([#b, #a, #d, #c]);
            """);
    }

    // Verify that we aggregate all matches, sorting by mixed indices.
    @Test public void testAggregate_indexedList_mixed() {
        test("testAggregate_indexedList_strings");
        var source = """
            transient A;
            A(#a, "2");
            A(#b, "1");
            A(#c, #one);
            A(#d, #two);
            B(indexedList(i, item)) :- A(item, i);
            """;
        // The order is unpredictable, but should be stable over time.
        check(execute(source)).eq("""
            define B/1;
            B([#b, #a, #c, #d]);
            """);
    }

    // Verify that we aggregate all matches.
    @Test public void testAggregate_list() {
        test("testAggregate_set");
        var source = """
            transient A;
            A(#a, 1);
            A(#b, 1);
            B(list(x)) :- A(_, x);
            """;
        check(execute(source)).eq("""
            define B/1;
            B([1, 1]);
            """);
    }

    // Verify that we aggregate all matches, and that a key with more than
    // one distinct value gets flagged as a `DUPLICATE_KEY`.
    @Test public void testAggregate_map() {
        test("testAggregate_set");
        var source = """
            transient A;
            A(#a, 1);
            A(#a, 2);
            A(#b, 2);
            A(#c, 3);
            B(map(k,v)) :- A(k, v);
            """;
        check(execute(source)).eq("""
            define B/1;
            B({#a: #duplicate_key, #b: 2, #c: 3});
            """);
    }

    // Verify that if there are matches but no numbers to take the maximum
    // of, we get no match.
    // of zero.
    @Test public void testAggregate_max_noNumericMatches() {
        test("testAggregate_max_noNumericMatches");
        var source = """
            A(#a);
            B(max(x)) :- A(x);
            """;
        check(execute(source)).eq("""
            define A/1;
            A(#a);
            """);
    }

    // Verify that we find the maximum of all numeric matches
    @Test public void testAggregate_max_numericMatches() {
        test("testAggregate_max_numericMatches");
        var source = """
            transient A;
            A(1);
            A(2);
            A(3);
            B(max(x)) :- A(x);
            """;
        check(execute(source)).eq("""
            define B/1;
            B(3);
            """);
    }

    // Verify that if there are matches but no numbers to take the minimum
    // of, we get no match.
    // of zero.
    @Test public void testAggregate_min_noNumericMatches() {
        test("testAggregate_min_noNumericMatches");
        var source = """
            A(#a);
            B(min(x)) :- A(x);
            """;
        check(execute(source)).eq("""
            define A/1;
            A(#a);
            """);
    }

    // Verify that we find the minimum of all numeric matches
    @Test public void testAggregate_min_numericMatches() {
        test("testAggregate_min_numericMatches");
        var source = """
            transient A;
            A(1);
            A(2);
            A(3);
            B(min(x)) :- A(x);
            """;
        check(execute(source)).eq("""
            define B/1;
            B(1);
            """);
    }

    // Verify that if there are matches but no numbers to sum, we get a sum
    // of zero.
    @Test public void testAggregate_sum_noNumericMatches() {
        test("testAggregate_sum_noNumericMatches");
        var source = """
            transient A;
            A(#a);
            B(sum(x)) :- A(x);
            """;
        check(execute(source)).eq("""
            define B/1;
            B(0);
            """);
    }

    // Verify that we sum all matches.
    @Test public void testAggregate_sum_numericMatches() {
        test("testAggregate_sum_numericMatches");
        var source = """
            transient A;
            A(#a, #foo);
            A(#a, 1);
            A(#b, 1);
            A(#a, 3);
            B(sum(x)) :- A(_, x);
            """;
        check(execute(source)).eq("""
            define B/1;
            B(5);
            """);
    }

    // Verify that we aggregate all matches.
    @Test public void testAggregate_set() {
        test("testAggregate_set");
        var source = """
            transient A;
            A(#a, 1);
            A(#a, 2);
            A(#b, 1);
            A(#c, 1);
            B(set(x)) :- A(x, _);
            """;
        check(execute(source)).eq("""
            define B/1;
            B({#a, #b, #c});
            """);
    }

    //-------------------------------------------------------------------------
    // Known vs. Inferred Facts

    // Facts inferred from axioms and rules are included in
    // the engine's set of inferred facts.  External facts are not.
    @Test public void testKnownVsInferred() {
        test("testKnownVsInferred");

        Set<Fact> facts = Set.of(
            new ListFact("Owns", List.of("joe", "car")),
            new ListFact("Owns", List.of("joe", "truck"))
        );
        var source = """
            Owner("joe");
            Thing(x) :- Owns(_, x);
            """;

        // Show all known facts
        check(execute(source, facts)).eq("""
            define Owner/1;
            Owner("joe");
            
            define Owns/2;
            Owns("joe", "car");
            Owns("joe", "truck");
            
            define Thing/1;
            Thing("car");
            Thing("truck");
            """);

        // Omit `Owns` facts, which were provided as inputs.
        check(infer(source, facts)).eq("""
            define Owner/1;
            Owner("joe");
            
            define Thing/1;
            Thing("car");
            Thing("truck");
            """);
    }

    //-------------------------------------------------------------------------
    // Bug fixes

    // Nero::asNeroScript emitted this NullPointerException:
    // Cannot invoke "com.wjduquette.joe.nero.Shape.toSpec" because the
    // return value of "com.wjduquette.joe.nero.Scheme.get(String)" is null
    //
    // The error was that `FactSet::getRelations` returned the raw
    // index key set, rather than the relations for which facts actually
    // exist.
    @Test public void testBug_asNeroScript_20250809() {
        test("testBug_asNeroScript_20250809");

        var source = """
            Owner("joe");
            Thing(x) :- Owns(_, x);
            """;

        // Show all known facts
        check(execute(source)).eq("""
            define Owner/1;
            Owner("joe");
            """);
    }

    //-------------------------------------------------------------------------
    // Helpers

    private enum Topic { THIS, THAT }

    // Execute the source, returning a Nero script of known facts.
    private String execute(String source) {
        try {
            var engine = nero.execute(new SourceBuffer("-", source));
            return nero.asNeroScript(engine.getKnownFacts());
        } catch (SyntaxError ex) {
            println(ex.getErrorReport());
            throw ex;
        }
    }

    // Execute the source given the input facts, returning a Nero script of
    // known facts.
    private String execute(String source, Set<Fact> facts) {
        var db = new FactSet(facts);
        try {
            var engine = nero.execute(new SourceBuffer("-", source), db);
            return nero.asNeroScript(engine.getKnownFacts());
        } catch (SyntaxError ex) {
            println(ex.getErrorReport());
            throw ex;
        }
    }

    // Execute the source given the input facts, returning a Nero script of
    // inferred facts.
    private String infer(String source, Set<Fact> facts) {
        var db = new FactSet(facts);
        try {
            var engine = nero.execute(new SourceBuffer("-", source), db);
            var factSet = new FactSet(engine.getInferredFacts());
            return nero.asNeroScript(factSet);
        } catch (SyntaxError ex) {
            println(ex.getErrorReport());
            throw ex;
        }
    }

    // Execute the source given the facts, returning a sorted list
    // of naive string representations for the observed facts.  We use
    // this when the use of Java data types prevents the result
    // from being represented as a Nero script
    private String inferRaw(String source, Set<Fact> facts) {
        var db = new FactSet(facts);
        var engine = nero.execute(new SourceBuffer("-", source), db);
        return engine.getInferredFacts().getAll().stream()
            .map(Fact::toString)
            .sorted()
            .collect(Collectors.joining("\n")) + "\n";
    }
}
