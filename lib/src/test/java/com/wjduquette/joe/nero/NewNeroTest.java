package com.wjduquette.joe.nero;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.Keyword;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.Ted;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.wjduquette.joe.checker.Checker.check;
import static com.wjduquette.joe.checker.Checker.checkThrow;

/**
 * Tests for the NewNero class.
 */
public class NewNeroTest extends Ted {
    private final Joe joe = new Joe();

    //-------------------------------------------------------------------------
    // Parsing

    // Verify that we get a rule set.
    @Test public void testParse_good() {
        test("testParse_good");
        var ruleset = NewNero.parse(buffer("A(1);"));

        check(ruleset.rules().isEmpty()).eq(true);
        check(ruleset.axioms().size()).eq(1);
    }

    // Verify that we get errors.
    @Test public void testParse_bad() {
        test("testParse_bad");
        checkThrow(() -> NewNero.parse(buffer("A(1;")))
            .containsString("Error in Nero input");
    }

    //-------------------------------------------------------------------------
    // Compilation

    // Verify that we get a rule set.
    @Test public void testCompile_good() {
        test("testCompile_good");
        var ruleset = NewNero.compile(buffer("A(1);"));

        check(ruleset.rules().isEmpty()).eq(true);
        check(ruleset.axioms().size()).eq(1);
    }

    // Verify that we get errors.
    @Test public void testCompile_syntax() {
        test("testCompile_syntax");
        checkThrow(() -> NewNero.compile(buffer("A(1;")))
            .containsString("Error in Nero input");
    }

    // Verify that we get errors.
    @Test public void testCompile_unstratifiable() {
        test("testCompile_unstratifiable");
        checkThrow(() -> NewNero.compile(buffer("""
            A(x) :- B(x), not C(x);
            C(x) :- A(x);
            """)))
            .containsString("Nero rule set cannot be stratified.");
    }

    //------------------------------------------------------------------------
    // with(): execution pipeline
    //
    // We test `with(String)` only, as it invokes `with(Joe, String)` which
    // invokes `with(Joe, NeroRuleSet`).  The other `with()` flavors are
    // trivial.

    @Test public void testWith_noInputs() {
        test("testWith_noInputs");
        var script = """
            A(1);
            """;
        check(NewNero.toNeroScript(NewNero.with(script).debug().infer())).eq("""
            define A/1;
            A(1);
            """);
    }

    @Test public void testWith_inputs() {
        test("testWith_inputs");
        var db = new FactSet();
        db.add(new ListFact("A", List.of(1.0)));
        var script = """
            B(2);
            """;
        check(NewNero.toNeroScript(NewNero.with(script).debug().infer(db))).eq("""
            define B/1;
            B(2);
            """);
        check(NewNero.toNeroScript(db)).eq("""
            define A/1;
            A(1);
            
            define B/1;
            B(2);
            """);
    }

    //------------------------------------------------------------------------
    // toNeroScript

    @Test
    public void testToNeroScript() {
        test("testToNeroScript");
        var script = """
            define Person/name,age;
            Person("Joe", 90);
            
            define Place/...;
            Place(attire: "Stetson", name: "Texas");
            
            define Thing/2;
            Thing("hat", "black");
            """;
        var facts = NewNero.with(script).infer();
        check(NewNero.toNeroScript(facts)).eq("""
            define Person/name,age;
            Person("Joe", 90);
            
            define Place/...;
            Place(attire: "Stetson", name: "Texas");
            
            define Thing/2;
            Thing("hat", "black");
            """);
    }

    //-------------------------------------------------------------------------
    // toNeroAxiom

    @Test
    public void testToNeroAxiom_listFact() {
        test("testToNeroAxiom_listFact");
        var fact = new ListFact("Thing", List.of("car", "red"));
        check(NewNero.toNeroAxiom(joe, fact))
            .eq("Thing(\"car\", \"red\");");
    }

    @Test
    public void testToNeroAxiom_mapFact() {
        test("testToNeroAxiom_mapFact");
        var fact = new MapFact("Thing", Map.of("id", "car", "color", "red"));
        check(NewNero.toNeroAxiom(joe, fact))
            .eq("Thing(color: \"red\", id: \"car\");");
    }

    @Test
    public void testToNeroAxiom_pairFact() {
        test("testToNeroAxiom_pairFact");
        var fact = new PairFact("Thing",
            List.of("id", "color"), List.of("car", "red"));
        check(NewNero.toNeroAxiom(joe, fact))
            .eq("Thing(\"car\", \"red\");");
    }

    //-------------------------------------------------------------------------
    // toNeroTerm

    @Test
    public void testToNeroTerm() {
        test("testToNeroTerm");
        check(NewNero.toNeroTerm(joe, null)).eq("null");
        check(NewNero.toNeroTerm(joe, true)).eq("true");
        check(NewNero.toNeroTerm(joe, false)).eq("false");
        check(NewNero.toNeroTerm(joe, 5.0)).eq("5");
        check(NewNero.toNeroTerm(joe, 5.1)).eq("5.1");
        check(NewNero.toNeroTerm(joe, new Keyword("id"))).eq("#id");
        check(NewNero.toNeroTerm(joe, "abc")).eq("\"abc\"");

        checkThrow(() -> NewNero.toNeroTerm(joe, this))
            .containsString("Non-Nero term:");
    }


    //------------------------------------------------------------------------
    // Helpers

    private SourceBuffer buffer(String script) {
        return new SourceBuffer("*test*", script);
    }
}
