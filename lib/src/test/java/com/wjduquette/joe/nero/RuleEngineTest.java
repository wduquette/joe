package com.wjduquette.joe.nero;

import com.wjduquette.joe.Keyword;
import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.Ted;
import org.junit.Before;
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
    private final Equivalence upper2lower =
        new LambdaEquivalence(new Keyword("upper2lower"),
            a -> a instanceof String s ? s.toLowerCase() : null,
            b -> b instanceof String s ? s.toUpperCase() : null
        );
    private Nero nero;

    @Before
    public void setup() {
        nero = new Nero();
        nero.addEquivalence(upper2lower);
    }
    //-------------------------------------------------------------------------
    // Stratification Conditions

    // Unstratifiable due to negation.
    @Test public void testUnstratified_negation() {
        test("testUnstratified_negation");

        var source = """
            define A/x;
            define C/x;
            A(x) :- B(x), not C(x);
            C(x) :- A(x);
            """;
        checkThrow(() -> execute(source))
            .containsString("Nero rule set cannot be stratified.");
    }

    @Test public void testUnstratified_list_literal() {
        test("testUnstratified_list_literal");

        var source = """
            define A/x;
            define B/x;
            A([x]) :- B(x);
            B(x) :- A(x);
            """;
        checkThrow(() -> execute(source))
            .containsString("Nero rule set cannot be stratified.");
    }

    @Test public void testUnstratified_map_literal() {
        test("testUnstratified_map_literal");

        var source = """
            define A/x;
            define B/x;
            A({#id: x}) :- B(x);
            B(x) :- A(x);
            """;
        checkThrow(() -> execute(source))
            .containsString("Nero rule set cannot be stratified.");
    }

    @Test public void testUnstratified_set_literal() {
        test("testUnstratified_set_literal");

        var source = """
            define A/x;
            define B/x;
            A({x}) :- B(x);
            B(x) :- A(x);
            """;
        checkThrow(() -> execute(source))
            .containsString("Nero rule set cannot be stratified.");
    }

    @Test public void testUnstratified_aggregator() {
        test("testUnstratified_aggregator");

        var source = """
            define A/x;
            define B/x;
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
            define Parent/p,c;
            define Ancestor/a,d;
            Parent(#walker, #bert);
            Parent(#bert, #clark);
            Ancestor(x, y) :- Parent(x, y);
            Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
            """;
        check(execute(source)).eq("""
            define Ancestor/a,d;
            Ancestor(#bert, #clark);
            Ancestor(#walker, #bert);
            Ancestor(#walker, #clark);
            
            define Parent/p,c;
            Parent(#bert, #clark);
            Parent(#walker, #bert);
            """);
    }

    // Verify that named atoms can be used as axioms, head atoms,
    // and body atoms.
    @Test public void testSimple_namedAtoms() {
        test("testSimple_named");
        var source = """
            define Parent/...;
            define Ancestor/...;
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

    // Verify that a named atom can be used in an axiom to create an
    // ordered fact.
    @Test public void testSimple_namedAxiom_ordered() {
        test("testSimple_namedAxiom_ordered");
        var source = """
            define Foo/a, b, c;
            Foo(c: #c, a: #a);
            """;
        check(execute(source)).eq("""
            define Foo/a,b,c;
            Foo(#a, null, #c);
            """);
    }

    // Verify that a named atom can be used in a rule head to create an
    // ordered fact.
    @Test public void testSimple_namedRule_ordered() {
        test("testSimple_namedRule_ordered");
        var source = """
            define transient Bar/a,c;
            define Foo/a,b,c;
            Bar(#a, #c);
            Foo(c: y, a: x) :- Bar(x, y);
            """;
        check(execute(source)).eq("""
            define Foo/a,b,c;
            Foo(#a, null, #c);
            """);
    }

    //-------------------------------------------------------------------------
    // Negation

    // Test that body atoms can be negated.
    @Test public void testNegation() {
        test("testNegation");
        var source = """
            define Thing/id;
            define Location/thing,place;
            define Homeless/thing;
            Thing(#desk);
            Thing(#pen);
            Location(#desk, #office);
            Homeless(x) :- Thing(x), not Location(x, _);
            """;
        check(execute(source)).eq("""
            define Homeless/thing;
            Homeless(#pen);
            
            define Location/thing,place;
            Location(#desk, #office);
            
            define Thing/id;
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
            define Thing/id,size;
            Thing(#pen,     1);
            Thing(#desk,    10);
            Thing(#whatsit, #unknown);
            
            // #whatsit is neither large nor small
            define Small/id;
            Small(x) :- Thing(x, size) where size < 5;
            define Large/id;
            Large(x) :- Thing(x, size) where size > 5;
            """;
        check(execute(source)).eq("""
            define Large/id;
            Large(#desk);
            
            define Small/id;
            Small(#pen);
            
            define Thing/id,size;
            Thing(#desk, 10);
            Thing(#pen, 1);
            Thing(#whatsit, #unknown);
            """);
    }

    // Verifies constraint execution given multiple constraint.
    @Test public void testConstraint_double() {
        test("testConstraint_double");
        var source = """
            define Thing/id,size;
            Thing(#pen,     1);
            Thing(#desk,    10);
            Thing(#whatsit, #unknown);
            
            // #whatsit is neither large nor small
            Thing(#pen,     1);
            Thing(#table,   10);
            Thing(#desk,    10);
            Thing(#whatsit, #unknown);
            
            // #whatsit is neither large nor small
            define AsBigAs/thing1,thing2;
            AsBigAs(x, y) :- Thing(x, xs), Thing(y, ys)
                where xs >= ys, x != y;
            """;
        check(execute(source)).eq("""
            define AsBigAs/thing1,thing2;
            AsBigAs(#desk, #pen);
            AsBigAs(#desk, #table);
            AsBigAs(#table, #desk);
            AsBigAs(#table, #pen);
            
            define Thing/id,size;
            Thing(#desk, 10);
            Thing(#pen, 1);
            Thing(#table, 10);
            Thing(#whatsit, #unknown);
            """);
    }

    @Test public void testConstraint_operators() {
        test("testConstraint_operators");
        var source = """
            define transient A/x;
            A(2);
            
            define transient B/x;
            B(1);
            B(2);
            B(3);
            
            define EQ/x,y;
            EQ(x,y) :- A(x), B(y) where x == y;
            
            define NE/x,y;
            NE(x,y) :- A(x), B(y) where x != y;
            
            define GT/x,y;
            GT(x,y) :- A(x), B(y) where x > y;
            
            define GE/x,y;
            GE(x,y) :- A(x), B(y) where x >= y;
            
            define LT/x,y;
            LT(x,y) :- A(x), B(y) where x < y;
            
            define LE/x,y;
            LE(x,y) :- A(x), B(y) where x <= y;
            """;
        check(execute(source)).eq("""
            define EQ/x,y;
            EQ(2, 2);
            
            define GE/x,y;
            GE(2, 1);
            GE(2, 2);
            
            define GT/x,y;
            GT(2, 1);
            
            define LE/x,y;
            LE(2, 2);
            LE(2, 3);
            
            define LT/x,y;
            LT(2, 3);
            
            define NE/x,y;
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
            define Pair/x,y;
            define Twin/x;
            Pair(#a, #b);
            Pair(#c, #c);
            Twin(x) :- Pair(x, x);
            """;
        check(execute(source)).eq("""
            define Pair/x,y;
            Pair(#a, #b);
            Pair(#c, #c);
            
            define Twin/x;
            Twin(#c);
            """);
    }

    @Test
    public void testKeywordMatchesEnum() {
        test("testKeywordMatchesEnum");
        Set<Fact> facts = Set.of(
            new Fact("Topic", List.of("x", "y"), List.of(Topic.THIS, "abc")),
            new Fact("Topic", List.of("x", "y"), List.of(Topic.THAT, "def"))
        );
        var source = """
            define Match/x;
            Match(x) :- Topic(#this, x);
            """;
        check(inferRaw(source, facts)).eq("""
            Fact[Match/x, {x=abc}]
            """);
    }

    //-------------------------------------------------------------------------
    // Fact Creation and `define` declarations

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
            define A/x;
            define B/x;
            transient A;
            A(#a);
            B(#b);
            """;
        check(execute(source)).eq("""
            define B/x;
            B(#b);
            """);
    }

    // Facts inferred by rules for transient relations are not retained.
    @Test public void testTransient_rule() {
        test("testTransient_rule");

        var source = """
            define A/x;
            define B/x;
            define C/x;
            transient B;
            A(#a);
            B(x) :- A(x);
            C(x) :- B(x);
            """;
        check(execute(source)).eq("""
            define A/x;
            A(#a);
            
            define C/x;
            C(#a);
            """);
    }

    // `define transient` also triggers transience.
    @Test public void testTransient_define() {
        test("testTransient_define");

        var source = """
            define transient A/x;
            define transient B/x;
            define C/x;
            A(#a);
            B(x) :- A(x);
            C(x) :- B(x);
            """;
        check(execute(source)).eq("""
            define C/x;
            C(#a);
            """);
    }

    //-------------------------------------------------------------------------
    // Updating Semantics

    @Test public void testUpdating_axioms() {
        test("testUpdating_axioms");
        var source = """
            define A/x;
            A(#a);
            define A!/x,y;
            A!(#b, #c);
            """;
        check(execute(source)).eq("""
            define A/x,y;
            A(#b, #c);
            """);
    }

    @Test public void testUpdating_rules() {
        test("testUpdating_rules");
        var source = """
            define A/x,y;
            A(#a, 5);
            A(#b, 7);
            
            define A!/x;
            A!(x) :- A(x, _);
            """;
        check(execute(source)).eq("""
            define A/x;
            A(#a);
            A(#b);
            """);
    }

    //-------------------------------------------------------------------------
    // Built-in _predicates

    @Test public void testBuiltIn_member_disaggregate() {
        test("testBuiltIn_member_disaggregate");
        var source = """
            define transient Owner/id,list;
            define Owns/id,item;
            Owner(#joe, [#hat, #boots, #truck]);
            Owns(id, item) :- Owner(id, list), member(item, list);
            """;
        check(execute(source)).eq("""
            define Owns/id,item;
            Owns(#joe, #boots);
            Owns(#joe, #hat);
            Owns(#joe, #truck);
            """);
    }

    @Test public void testBuiltIn_member_match() {
        test("testBuiltIn_member_match");
        var source = """
            define transient Owner/id,list;
            define OwnsHat/id;
            Owner(#joe, [#hat, #boots, #truck]);
            OwnsHat(id) :- Owner(id, list), member(#hat, list);
            """;
        check(execute(source)).eq("""
            define OwnsHat/id;
            OwnsHat(#joe);
            """);
    }

    @Test public void testBuiltIn_member_noCollection() {
        test("testBuiltIn_member_noCollection");
        var source = """
            define transient Owner/id,list;
            define Owns/id,item;
            Owner(#joe, #notCollection);
            Owns(id, item) :- Owner(id, list), member(item, list);
            """;
        check(execute(source)).eq("""
            """);
    }

    @Test public void testBuiltIn_indexedMember_disaggregate() {
        test("testBuiltIn_indexedMember_disaggregate");
        var source = """
            define transient Owner/id,list;
            define Owns/id,index,item;
            Owner(#joe, [#hat, #boots, #truck]);
            Owns(id, i, item) :- Owner(id, list), indexedMember(i, item, list);
            """;
        check(execute(source)).eq("""
            define Owns/id,index,item;
            Owns(#joe, 0, #hat);
            Owns(#joe, 1, #boots);
            Owns(#joe, 2, #truck);
            """);
    }

    @Test public void testBuiltIn_indexedMember_match() {
        test("testBuiltIn_indexedMember_match");
        var source = """
            define transient Owner/id,list;
            define OwnsHat/id,index;
            Owner(#joe, [#hat, #boots, #truck]);
            OwnsHat(id, i) :- Owner(id, list), indexedMember(i, #hat, list);
            """;
        check(execute(source)).eq("""
            define OwnsHat/id,index;
            OwnsHat(#joe, 0);
            """);
    }

    @Test public void testBuiltIn_indexedMember_noCollection() {
        test("testBuiltIn_indexedMember_noCollection");
        var source = """
            define transient Owner/id,list;
            define Owns/id,index,item;
            Owner(#joe, #notCollection);
            Owns(id, i, item) :- Owner(id, list), indexedMember(i, item, list);
            """;
        check(execute(source)).eq("""
            """);
    }

    @Test public void testBuiltIn_keyedMember_disaggregate() {
        test("testBuiltIn_keyedMember_disaggregate");
        var source = """
            define transient Owner/id,map;
            define Wears/id,k,v;
            Owner(#joe, {#head: #hat, #feet: #boots});
            Wears(id, k, v) :- Owner(id, map), keyedMember(k, v, map);
            """;
        check(execute(source)).eq("""
            define Wears/id,k,v;
            Wears(#joe, #feet, #boots);
            Wears(#joe, #head, #hat);
            """);
    }

    @Test public void testBuiltIn_keyedMember_match() {
        test("testBuiltIn_keyedMember_match");
        var source = """
            define transient Owner/id,map;
            define WearsHat/id,k;
            Owner(#joe, {#head: #hat, #feet: #boots});
            WearsHat(id, k) :- Owner(id, map), keyedMember(k, #hat, map);
            """;
        check(execute(source)).eq("""
            define WearsHat/id,k;
            WearsHat(#joe, #head);
            """);
    }

    @Test public void testBuiltIn_keyedMember_noCollection() {
        test("testBuiltIn_keyedMember_noCollection");
        var source = """
            define transient Owner/id,map;
            define Wears/id,k,v;
            Owner(#joe, #notCollection);
            Wears(id, k, v) :- Owner(id, map), keyedMember(k, v, map);
            """;
        check(execute(source)).eq("""
            """);
    }

    @Test public void testBuiltIn_equivalent_AB() {
        test("testBuiltIn_keyedMember_equivalent_AB");
        var source = """
            define transient Data/s,n;
            define Got/s,n;
            Data("1", 1);    // Equivalent
            Data("2", 3);    // Not equivalent
            Data("XYZ", 4);  // Not equivalent
            Got(s, n) :- Data(s, n), equivalent(#str2num, s, n);
            """;
        check(execute(source)).eq("""
            define Got/s,n;
            Got("1", 1);
            """);
    }

    @Test public void testBuiltIn_equivalent_A() {
        test("testBuiltIn_keyedMember_equivalent_A");
        var source = """
            define transient Data/s;
            define Got/s,n;
            Data("1");    // Equivalent
            Data("XYZ");  // Not equivalent
            Got(s, n) :- Data(s), equivalent(#str2num, s, n);
            """;
        check(execute(source)).eq("""
            define Got/s,n;
            Got("1", 1);
            """);
    }

    @Test public void testBuiltIn_equivalent_B() {
        test("testBuiltIn_keyedMember_equivalent_A");
        var source = """
            define transient Data/n;
            define Got/s,n;
            Data(1);    // Equivalent
            Data(#foo); // Not equivalent
            Got(s, n) :- Data(n), equivalent(#str2num, s, n);
            """;
        check(execute(source)).eq("""
            define Got/s,n;
            Got("1", 1);
            """);
    }

    @Test public void testBuiltIn_equivalent_registered() {
        var source = """
            define transient Upper/x;
            define transient Lower/x;
            define transient Both/x,y;
            define Got/upper,lower;
            Upper("ABC");
            Upper(123);
            Lower("xyz");
            Lower(456);
            Both("DEF", "def");
            Both("GHI", "xyz");
            Both("GHI", 123);
            
            Got(u, l) :- Upper(u), equivalent(#upper2lower, u, l);
            Got(u, l) :- Lower(l), equivalent(#upper2lower, u, l);
            Got(u, l) :- Both(u, l), equivalent(#upper2lower, u, l);
            """;

        // Execute with #upper2lower
        check(execute(source)).eq("""
            define Got/upper,lower;
            Got("ABC", "abc");
            Got("DEF", "def");
            Got("XYZ", "xyz");
            """);
    }

    @Test public void testBuiltIn_negation() {
        test("testBuiltIn_negation");
        var source = """
            define transient Item/id;
            Item(#hat);
            Item(#boots);
            Item(#truck);
            Item(#car);
            define transient Owner/id,list;
            Owner(#joe, [#hat, #boots, #truck]);
            
            define NotOwns/owner,item;
            NotOwns(id, item) :- Owner(id, list), Item(item), not member(item, list);
            """;
        check(execute(source)).eq("""
            define NotOwns/owner,item;
            NotOwns(#joe, #car);
            """);
    }

    //-------------------------------------------------------------------------
    // Collection Literals

    // Verify that list literals can be used in axioms.
    @Test public void testListLiteral_axioms() {
        test("testListLiteral_axioms");
        var source = """
            define A/list;
            A([]);
            A([#a, 5]);
            """;
        check(execute(source)).eq("""
            define A/list;
            A([#a, 5]);
            A([]);
            """);
    }

    // Verify that list literals can be used in rules.
    @Test public void testListLiteral_rules() {
        test("testListLiteral_rules");
        var source = """
            define transient A/x,y;
            define B/x;
            define C/x, y;
            A(#a, 5);
            B([x, y]) :- A(x, y);
            C(x, []) :- A(x, _);
            """;
        check(execute(source)).eq("""
            define B/x;
            B([#a, 5]);
            
            define C/x,y;
            C(#a, []);
            """);
    }

    // Verify that map literals can be used in axioms.
    @Test public void testMapLiteral_axioms() {
        test("testMapLiteral_axioms");
        var source = """
            define A/map;
            A({:});
            A({#a: 5});
            """;
        check(execute(source)).eq("""
            define A/map;
            A({#a: 5});
            A({:});
            """);
    }

    // Verify that map literals can be used in rules.
    @Test public void testMapLiteral_rules() {
        test("testMapLiteral_rules");
        var source = """
            define transient A/x,y;
            define B/x;
            define C/x,y;
            A(#a, 5);
            B({x: y}) :- A(x, y);
            C(x, {:}) :- A(x, _);
            """;
        check(execute(source)).eq("""
            define B/x;
            B({#a: 5});
            
            define C/x,y;
            C(#a, {:});
            """);
    }

    // Verify that set literals can be used in axioms.
    @Test public void testSetLiteral_axioms() {
        test("testSetLiteral_axioms");
        var source = """
            define A/set;
            A({});
            A({#a, 5});
            """;
        check(execute(source)).eq("""
            define A/set;
            A({#a, 5});
            A({});
            """);
    }

    // Verify that set literals can be used in rules.
    @Test public void testSetLiteral_rules() {
        test("testSetLiteral_rules");
        var source = """
            define transient A/x,y;
            define B/x;
            define C/x,y;
            A(#a, 5);
            B({x, y}) :- A(x, y);
            C(x, {}) :- A(x, _);
            """;
        check(execute(source)).eq("""
            define B/x;
            B({#a, 5});
            
            define C/x,y;
            C(#a, {});
            """);
    }

    //-------------------------------------------------------------------------
    // Aggregation Functions

    // Verify that we aggregate all matches, sorting by numeric indices.
    @Test public void testAggregate_indexedList_numbers() {
        test("testAggregate_indexedList_numbers");
        var source = """
            define transient A/x,y;
            define B/list;
            A(#a, 2);
            A(#b, 1);
            A(#c, 4);
            A(#d, 3);
            B(indexedList(i, item)) :- A(item, i);
            """;
        check(execute(source)).eq("""
            define B/list;
            B([#b, #a, #d, #c]);
            """);
    }

    // Verify that we aggregate all matches, sorting by string indices.
    @Test public void testAggregate_indexedList_strings() {
        test("testAggregate_indexedList_strings");
        var source = """
            define transient A/x,y;
            define B/list;
            A(#a, "2");
            A(#b, "1");
            A(#c, "4");
            A(#d, "3");
            B(indexedList(i, item)) :- A(item, i);
            """;
        check(execute(source)).eq("""
            define B/list;
            B([#b, #a, #d, #c]);
            """);
    }

    // Verify that we aggregate all matches, sorting by mixed indices.
    @Test public void testAggregate_indexedList_mixed() {
        test("testAggregate_indexedList_strings");
        var source = """
            define transient A/x,y;
            define B/list;
            A(#a, "2");
            A(#b, "1");
            A(#c, #one);
            A(#d, #two);
            B(indexedList(i, item)) :- A(item, i);
            """;
        // The order is unpredictable, but should be stable over time.
        check(execute(source)).eq("""
            define B/list;
            B([#b, #a, #c, #d]);
            """);
    }

    // Verify that we aggregate all matches.
    @Test public void testAggregate_list() {
        test("testAggregate_set");
        var source = """
            define transient A/x,y;
            define B/list;
            A(#a, 1);
            A(#b, 1);
            B(list(x)) :- A(_, x);
            """;
        check(execute(source)).eq("""
            define B/list;
            B([1, 1]);
            """);
    }

    // Verify that we aggregate all matches, and that a key with more than
    // one distinct value gets flagged as a `DUPLICATE_KEY`.
    @Test public void testAggregate_map() {
        test("testAggregate_set");
        var source = """
            define transient A/x,y;
            define B/map;
            A(#a, 1);
            A(#a, 2);
            A(#b, 2);
            A(#c, 3);
            B(map(k,v)) :- A(k, v);
            """;
        check(execute(source)).eq("""
            define B/map;
            B({#a: #duplicateKey, #b: 2, #c: 3});
            """);
    }

    // Verify that if there are matches but no numbers to take the maximum
    // of, we get no match.
    // of zero.
    @Test public void testAggregate_max_noNumericMatches() {
        test("testAggregate_max_noNumericMatches");
        var source = """
            define A/x;
            define B/max;
            A(#a);
            B(max(x)) :- A(x);
            """;
        check(execute(source)).eq("""
            define A/x;
            A(#a);
            """);
    }

    // Verify that we find the maximum of all numeric matches
    @Test public void testAggregate_max_numericMatches() {
        test("testAggregate_max_numericMatches");
        var source = """
            define transient A/x;
            define B/x;
            A(1);
            A(2);
            A(3);
            B(max(x)) :- A(x);
            """;
        check(execute(source)).eq("""
            define B/x;
            B(3);
            """);
    }

    // Verify that if there are matches but no numbers to take the minimum
    // of, we get no match.
    // of zero.
    @Test public void testAggregate_min_noNumericMatches() {
        test("testAggregate_min_noNumericMatches");
        var source = """
            define A/x;
            define B/x;
            A(#a);
            B(min(x)) :- A(x);
            """;
        check(execute(source)).eq("""
            define A/x;
            A(#a);
            """);
    }

    // Verify that we find the minimum of all numeric matches
    @Test public void testAggregate_min_numericMatches() {
        test("testAggregate_min_numericMatches");
        var source = """
            define transient A/x;
            define B/x;
            A(1);
            A(2);
            A(3);
            B(min(x)) :- A(x);
            """;
        check(execute(source)).eq("""
            define B/x;
            B(1);
            """);
    }

    // Verify that if there are matches but no numbers to sum, we get a sum
    // of zero.
    @Test public void testAggregate_sum_noNumericMatches() {
        test("testAggregate_sum_noNumericMatches");
        var source = """
            define transient A/x;
            define B/x;
            A(#a);
            B(sum(x)) :- A(x);
            """;
        check(execute(source)).eq("""
            define B/x;
            B(0);
            """);
    }

    // Verify that we sum all matches.
    @Test public void testAggregate_sum_numericMatches() {
        test("testAggregate_sum_numericMatches");
        var source = """
            define transient A/x,y;
            define B/x;
            A(#a, #foo);
            A(#a, 1);
            A(#b, 1);
            A(#a, 3);
            B(sum(x)) :- A(_, x);
            """;
        check(execute(source)).eq("""
            define B/x;
            B(5);
            """);
    }

    // Verify that we aggregate all matches.
    @Test public void testAggregate_set() {
        test("testAggregate_set");
        var source = """
            define transient A/x,y;
            define B/x;
            A(#a, 1);
            A(#a, 2);
            A(#b, 1);
            A(#c, 1);
            B(set(x)) :- A(x, _);
            """;
        check(execute(source)).eq("""
            define B/x;
            B({#a, #b, #c});
            """);
    }

    // Aggregation can fail if the set of bindings includes variables not
    // in the head of the rule.  In this case the bindings include the
    // "ids" variable, whose value differs for every match.
    //
    // This test case reflects the rule set in which the error was initially
    // seen; the error can be reproduced without using a built-in-predicate
    // in the rule's body.
    @Test public void testAggregation_complex() {
        test("testAggregate_set");
        var source = """
            define transient Event/ids,t;
            Event({#a,#b}, 1);
            Event({#a,#c}, 2);
            Event({#a,#d}, 3);
            Event({#b,#c}, 4);
            Event({#b,#d}, 5);
            Event({#b,#e}, 6);

            define Min/id,t;
            Min(id, min(t)) :- Event(ids, t), member(id, ids);
            """;
        check(execute(source)).eq("""
            define Min/id,t;
            Min(#a, 1);
            Min(#b, 1);
            Min(#c, 2);
            Min(#d, 3);
            Min(#e, 6);
            """);
    }

    //-------------------------------------------------------------------------
    // Pattern Terms
    //
    // NamedFieldPatterns and OrderedFieldPatterns are tested in
    // tests/type.joe.RuleSet.joe, since they require Joe values that have
    // no literals in Nero code.

    @Test public void testPattern_list() {
        test("testPattern_list");
        var source = """
            define transient A/x,y;
            define B/x,y,z;
            A(#a, [1, 2]);
            A(#b, [3, 4]);
            B(x, y, z) :- A(x, [y, z]);
            """;
        check(execute(source)).eq("""
            define B/x,y,z;
            B(#a, 1, 2);
            B(#b, 3, 4);
            """);
    }

    @Test public void testPattern_map() {
        test("testPattern_map");
        var source = """
            define transient A/x,y;
            define B/x,y,z;
            A(#a, {#a: 1, #b: 2});
            A(#b, {#a: 3, #b: 4});
            B(x, y, z) :- A(x, {#a: y, #b: z});
            """;
        check(execute(source)).eq("""
            define B/x,y,z;
            B(#a, 1, 2);
            B(#b, 3, 4);
            """);
    }

    @Test public void testPattern_typeName() {
        test("testPattern_typeName");
        var source = """
            define transient A/x,y;
            define B/x;
            A(#a, [1, 2]);
            A(#b, [3, 4]);
            A(#c, 5);
            B(x) :- A(x, List());
            """;
        check(execute(source)).eq("""
            define B/x;
            B(#a);
            B(#b);
            """);
    }

    @Test public void testPattern_subpattern_free() {
        test("testPattern_typeName");
        var source = """
            define transient A/x,y;
            define B/x,y;
            A(#a, [1, 2]);
            A(#b, [3, 4]);
            A(#c, 5);
            B(x, y) :- A(x, y@[_,_]);
            """;
        check(execute(source)).eq("""
            define B/x,y;
            B(#a, [1, 2]);
            B(#b, [3, 4]);
            """);
    }

    // Verify that a subpattern works just fine if the subpattern variable
    // is already bound, even if it would be a weird thing to do.
    @Test public void testPattern_subpattern_bound() {
        test("testPattern_typeName");
        var source = """
            define transient A/x,y;
            define B/x;
            A([1, 2], [1, 2]);
            A([3, 4], [3, 4]);
            A([5, 6], [7, 8]);
            B(x) :- A(x, x@[_,_]);
            """;
        check(execute(source)).eq("""
            define B/x;
            B([1, 2]);
            B([3, 4]);
            """);
    }

    //-------------------------------------------------------------------------
    // Known vs. Inferred Facts

    // Facts inferred from axioms and rules are included in
    // the engine's set of inferred facts.  External facts are not.
    @Test public void testKnownVsInferred() {
        test("testKnownVsInferred");

        Set<Fact> facts = Set.of(
            new Fact("Owns", List.of("owner", "thing"), List.of("joe", "car")),
            new Fact("Owns", List.of("owner", "thing"), List.of("joe", "truck"))
        );
        var source = """
            define Owner/owner;
            define Thing/thing;
            Owner("joe");
            Thing(x) :- Owns(_, x);
            """;

        // Show all known facts
        check(execute(source, facts)).eq("""
            define Owner/owner;
            Owner("joe");
            
            define Owns/owner,thing;
            Owns("joe", "car");
            Owns("joe", "truck");
            
            define Thing/thing;
            Thing("car");
            Thing("truck");
            """);

        // Omit `Owns` facts, which were provided as inputs.
        check(infer(source, facts)).eq("""
            define Owner/owner;
            Owner("joe");
            
            define Thing/thing;
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
            define Owner/owner;
            define Thing/thing;
            Owner("joe");
            Thing(x) :- Owns(_, x);
            """;

        // Show all known facts
        check(execute(source)).eq("""
            define Owner/owner;
            Owner("joe");
            """);
    }

    //-------------------------------------------------------------------------
    // Helpers

    private enum Topic { THIS, THAT }

    // Execute the source, returning a Nero script of known facts.
    private String execute(String source) {
        try {
            var db = nero.with(source).debug().infer();
            return nero.toNeroScript(db);
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
            nero.with(source).debug().update(db);
            return nero.toNeroScript(db);
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
            var inferred = nero.with(source).debug().update(db);
            return nero.toNeroScript(inferred);
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
        var inferred = nero.with(source).debug().update(db);
        return inferred.all().stream()
            .map(Fact::toString)
            .sorted()
            .collect(Collectors.joining("\n")) + "\n";
    }
}
