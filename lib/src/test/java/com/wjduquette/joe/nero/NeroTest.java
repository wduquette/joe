package com.wjduquette.joe.nero;

import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.Trace;
import com.wjduquette.joe.parser.ASTRuleSet;
import com.wjduquette.joe.parser.Parser;
import org.junit.Test;

import java.util.stream.Collectors;

import static com.wjduquette.joe.checker.Checker.check;

// Tests for the Nero engine. This test suite does NOT check for parsing
// errors.
public class NeroTest extends Ted {
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
            Parent(#walker, #bert);
            Parent(#bert, #clark);
            Ancestor(x, y) :- Parent(f0: x, f1: y);
            Ancestor(x, y) :- Parent(f0: x, f1: z), Ancestor(f0: z, f1: y);
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
    public void testUnstratified() {
        test("testUnstratified");
        var source = """
            P(x) :- R(x), not Q(x);
            Q(x) :- P(x);
            """;
        var nero = compile(source);
        check(nero.isStratified()).eq(false);
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

    //-------------------------------------------------------------------------
    // Helpers

    private boolean gotParseError = false;

    private Nero compile(String source) {
        gotParseError = false;
        var buff = new SourceBuffer("-", source);
        var ast = parse(buff);
        var ruleset = new RuleSetCompiler(ast).compile();
        return new Nero(ruleset);
    }

    public ASTRuleSet parse(SourceBuffer source) {
        var parser = new Parser(source, this::errorHandler);
        var ast = parser.parseNero();
        if (gotParseError) throw new JoeError("Error in Nero input.");
        return ast;
    }

    private void errorHandler(Trace trace, boolean incomplete) {
        gotParseError = true;
        System.out.println("line " + trace.line() + ": " +
            trace.message());
    }

    private String infer(String source) {
        var nero = compile(source);
        nero.infer();
        return nero.getAllFacts().stream()
            .map(Fact::toString)
            .sorted()
            .collect(Collectors.joining("\n")) + "\n";
    }

}
