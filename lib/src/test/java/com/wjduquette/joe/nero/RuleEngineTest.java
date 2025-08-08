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

// Tests for the Nero engine. This test suite does NOT check for parsing
// errors.
public class RuleEngineTest extends Ted {
    private final Nero nero = new Nero(new Joe());

    @Test
    public void testSimple() {
        test("testSimple");
        var source = """
            Parent(#walker, #bert);
            Parent(#bert, #clark);
            Ancestor(x, y) :- Parent(x, y);
            Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
            """;
        check(infer(source)).eq("""
            ListFact[relation=Ancestor, fields=[#bert, #clark]]
            ListFact[relation=Ancestor, fields=[#walker, #bert]]
            ListFact[relation=Ancestor, fields=[#walker, #clark]]
            ListFact[relation=Parent, fields=[#bert, #clark]]
            ListFact[relation=Parent, fields=[#walker, #bert]]
            """);
    }

    @Test
    public void testSimple_named() {
        test("testSimple_named");
        var source = """
            Parent(p: #walker, c: #bert);
            Parent(p: #bert, c: #clark);
            Ancestor(a: x, d: y) :- Parent(p: x, c: y);
            Ancestor(a: x, d: y) :- Parent(p: x, c: z), Ancestor(a: z, d: y);
            """;
        check(infer(source)).eq("""
            MapFact[relation=Ancestor, fieldMap={a=#bert, d=#clark}]
            MapFact[relation=Ancestor, fieldMap={a=#walker, d=#bert}]
            MapFact[relation=Ancestor, fieldMap={a=#walker, d=#clark}]
            MapFact[relation=Parent, fieldMap={p=#bert, c=#clark}]
            MapFact[relation=Parent, fieldMap={p=#walker, c=#bert}]
            """);
    }

    @Test
    public void testConstraint() {
        test("testConstraint");
        var source = """
            Thing(#pen,     1);
            Thing(#desk,    10);
            Thing(#whatsit, #unknown);
            
            // #whatsit is neither large nor small
            Small(x) :- Thing(x, size) where size < 5;
            Large(x) :- Thing(x, size) where size > 5;
            """;
        check(infer(source)).eq("""
            ListFact[relation=Large, fields=[#desk]]
            ListFact[relation=Small, fields=[#pen]]
            ListFact[relation=Thing, fields=[#desk, 10.0]]
            ListFact[relation=Thing, fields=[#pen, 1.0]]
            ListFact[relation=Thing, fields=[#whatsit, #unknown]]
            """);
    }

    @Test
    public void testNegation() {
        test("testNegation");
        var source = """
            Thing(#desk);
            Thing(#pen);
            Location(#desk, #office);
            Homeless(x) :- Thing(x), not Location(x, _);
            """;
        check(infer(source)).eq("""
            ListFact[relation=Homeless, fields=[#pen]]
            ListFact[relation=Location, fields=[#desk, #office]]
            ListFact[relation=Thing, fields=[#desk]]
            ListFact[relation=Thing, fields=[#pen]]
            """);
    }

    @Test
    public void testBindAndMatch() {
        test("testBindAndMatch");
        var source = """
            Pair(#a, #b);
            Pair(#c, #c);
            Twin(x) :- Pair(x, x);
            """;
        check(infer(source)).eq("""
            ListFact[relation=Pair, fields=[#a, #b]]
            ListFact[relation=Pair, fields=[#c, #c]]
            ListFact[relation=Twin, fields=[#c]]
            """);
    }

    @Test
    public void testStratified() {
        test("testStratified");
        var source = """
            // Transitive closure of connections in a directed graph.
            // Strata 0: CanGo
            // Strata 1: CantGo

            // There is a path from node x to node y.
            CanGo(x,y) :- Edge(x,y);
            CanGo(x,y) :- Edge(x,z), CanGo(z,y);

            // There is no path from node x to node y.
            CantGo(x,y) :- Node(x), Node(y), not CanGo(x,y);

            Node(#a);
            Node(#b);
            Node(#c);
            Edge(#a, #b);
            Edge(#b, #b);
            Edge(#b, #c);
            Edge(#c, #b);
            """;
        check(infer(source)).eq("""
            ListFact[relation=CanGo, fields=[#a, #b]]
            ListFact[relation=CanGo, fields=[#a, #c]]
            ListFact[relation=CanGo, fields=[#b, #b]]
            ListFact[relation=CanGo, fields=[#b, #c]]
            ListFact[relation=CanGo, fields=[#c, #b]]
            ListFact[relation=CanGo, fields=[#c, #c]]
            ListFact[relation=CantGo, fields=[#a, #a]]
            ListFact[relation=CantGo, fields=[#b, #a]]
            ListFact[relation=CantGo, fields=[#c, #a]]
            ListFact[relation=Edge, fields=[#a, #b]]
            ListFact[relation=Edge, fields=[#b, #b]]
            ListFact[relation=Edge, fields=[#b, #c]]
            ListFact[relation=Edge, fields=[#c, #b]]
            ListFact[relation=Node, fields=[#a]]
            ListFact[relation=Node, fields=[#b]]
            ListFact[relation=Node, fields=[#c]]
            """);
    }

    @Test
    public void testPairFactCreation() {
        test("testPairFactCreation");
        var source = """
            define Pair/left, right;
            Pair(#c, #c);
            define Twin/id;
            Twin(x) :- Pair(x, x);
            """;
        check(infer(source)).eq("""
            PairFact[relation=Pair, fieldNames=[left, right], fields=[#c, #c]]
            PairFact[relation=Twin, fieldNames=[id], fields=[#c]]
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
        check(infer(source, facts)).eq("""
            ListFact[relation=Match, fields=[abc]]
            """);
    }

    //-------------------------------------------------------------------------
    // Transience

    @Test
    public void testTransient_transient_axiom() {
        test("testTransient_transient_axiom");

        var source = """
            transient A;
            A(#a);
            B(#b);
            """;
        check(infer(source)).eq("""
            ListFact[relation=B, fields=[#b]]
            """);
    }

    @Test
    public void testTransient_define_transient_axiom() {
        test("testTransient_define_transient_axiom");

        var source = """
            define transient A/1;
            A(#a);
            B(#b);
            """;
        check(infer(source)).eq("""
            ListFact[relation=B, fields=[#b]]
            """);
    }

    @Test
    public void testTransient_transient_rule() {
        test("testTransient_transient_axiom");

        var source = """
            transient B;
            A(#a);
            B(x) :- A(x);
            C(x) :- B(x);
            """;
        check(infer(source)).eq("""
            ListFact[relation=A, fields=[#a]]
            ListFact[relation=C, fields=[#a]]
            """);
    }

    @Test
    public void testTransient_define_transient_rule() {
        test("testTransient_define_transient_axiom");
        var source = """
            define transient B/1;
            A(#a);
            B(x) :- A(x);
            C(x) :- B(x);
            """;
        check(infer(source)).eq("""
            ListFact[relation=A, fields=[#a]]
            ListFact[relation=C, fields=[#a]]
            """);
    }

    //-------------------------------------------------------------------------
    // Updating Semantics

    @Test public void testUpdating_axioms() {
        test("testUpdating_axioms");
        var source = """
            A(#a);
            A!(#b);
            """;
        check(infer(source)).eq("""
            ListFact[relation=A, fields=[#b]]
            """);
    }

    @Test public void testUpdating_rules() {
        test("testUpdating_rules");
        var source = """
            A(#a, 5);
            A(#b, 7);
            A!(x) :- A(x, _);
            """;
        check(infer(source)).eq("""
            ListFact[relation=A, fields=[#a]]
            ListFact[relation=A, fields=[#b]]
            """);
    }

    //-------------------------------------------------------------------------
    // Helpers

    private enum Topic { THIS, THAT }

    private String infer(String source) {
        try {
            var engine = nero.execute(new SourceBuffer("-", source));
            return engine.getKnownFacts().getAll().stream()
                .map(Fact::toString)
                .sorted()
                .collect(Collectors.joining("\n")) + "\n";
        } catch (SyntaxError ex) {
            println(ex.getErrorReport());
            throw ex;
        }
    }

    private String infer(String source, Set<Fact> facts) {
        var db = new FactSet(facts);
        var engine = nero.execute(new SourceBuffer("-", source), db);
        return engine.getInferredFacts().stream()
            .map(Fact::toString)
            .sorted()
            .collect(Collectors.joining("\n")) + "\n";
    }
}
