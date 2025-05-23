package com.wjduquette.joe.nero;

import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.tools.nero.Compiler;
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
            Ancestor(#bert, #clark)
            Ancestor(#walker, #bert)
            Ancestor(#walker, #clark)
            Parent(#bert, #clark)
            Parent(#walker, #bert)
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
            Large(#desk)
            Small(#pen)
            Thing(#desk, 10.0)
            Thing(#pen, 1.0)
            Thing(#whatsit, #unknown)
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
            Homeless(#pen)
            Location(#desk, #office)
            Thing(#desk)
            Thing(#pen)
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
            Pair(#a, #b)
            Pair(#c, #c)
            Twin(#c)
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
            CanGo(#a, #b)
            CanGo(#b, #b)
            CanGo(#b, #c)
            CanGo(#c, #b)
            CantGo(#a, #a)
            CantGo(#b, #a)
            CantGo(#c, #a)
            Edge(#a, #b)
            Edge(#b, #b)
            Edge(#b, #c)
            Edge(#c, #b)
            Node(#a)
            Node(#b)
            Node(#c)
            """);
    }

    //-------------------------------------------------------------------------
    // Helpers

    private Nero compile(String source) {
        var buff = new SourceBuffer("-", source);
        var ruleset = new Compiler(buff).compile();
        return new Nero(ruleset);
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
