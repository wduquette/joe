package com.wjduquette.joe.nero;

import com.wjduquette.joe.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.wjduquette.joe.checker.Checker.*;

/**
 * Tests for the NewNero class.
 */
public class NeroTest extends Ted {
    private final Joe joe = new Joe();
    private Nero nero;

    @Before
    public void setup() {
        nero = new Nero(joe);
    }

    //-------------------------------------------------------------------------
    // Parsing

    // Verify that we get a rule set.
    @Test public void testParse_good() {
        test("testParse_good");
        var ruleset = Nero.parse(buffer("""
            define A/x;
            A(1);
            """));

        check(ruleset.rules().isEmpty()).eq(true);
        check(ruleset.axioms().size()).eq(1);
    }

    // Verify that we get errors.
    @Test public void testParse_bad() {
        test("testParse_bad");
        checkThrow(() -> Nero.parse(buffer("A(1;")))
            .containsString("Error in Nero input");
    }

    //-------------------------------------------------------------------------
    // Compilation

    // Verify that we get a rule set.
    @Test public void testCompile_good() {
        test("testCompile_good");
        var ruleset = Nero.parse(buffer("""
            define A/x;
            A(1);
            """));

        check(ruleset.rules().isEmpty()).eq(true);
        check(ruleset.axioms().size()).eq(1);
    }

    // Verify that we get errors.
    @Test public void testCompile_syntax() {
        test("testCompile_syntax");
        checkThrow(() -> Nero.compile(buffer("A(1);")))
            .containsString("Error in Nero input");
    }

    // Verify that we get stratification errors.
    @Test public void testCompile_unstratifiable() {
        test("testCompile_unstratifiable");
        checkThrow(() -> Nero.compile(buffer("""
            define A/x;
            define C/x;
            A(x) :- B(x), not C(x);
            C(x) :- A(x);
            """)))
            .containsString("Nero rule set cannot be stratified.");
    }


    //-------------------------------------------------------------------------
    // Schema

    @Test public void testSchema_static() {
        test("testSchema_static");
        var schema = Nero.schema("""
            define Foo/a,b;
            """);
        check(schema.isStatic()).eq(true);
    }

    @Test public void testSchema_nonStatic_axiom() {
        test("testSchema_nonStatic_axiom");
        var script = """
            define Foo/a,b;
            Foo(1,2);
            """;
        checkThrow(() -> Nero.schema(script))
            .containsString("Expected a static schema.");
    }

    @Test public void testSchema_nonStatic_rule() {
        test("testSchema_nonStatic_rule");
        var script = """
            define Foo/a,b;
            Foo(x,y) :- Bar(x,y);
            """;
        checkThrow(() -> Nero.schema(script))
            .containsString("Expected a static schema.");
    }

    @Test public void testSchema_nonStatic_transient() {
        test("testSchema_nonStatic_transient");
        var script = """
            define Foo/a,b;
            define transient Bar/a,b;
            """;
        checkThrow(() -> Nero.schema(script))
            .containsString("Expected a static schema.");
    }

    @Test public void testSchema_nonStatic_update() {
        test("testSchema_nonStatic_update");
        var script = """
            define Foo!/a,b;
            """;
        checkThrow(() -> Nero.schema(script))
            .containsString("Expected a static schema.");
    }

    //------------------------------------------------------------------------
    // with(): execution pipeline
    //
    // We test `with(String)` only, as it invokes `with(Joe, String)` which
    // invokes `with(Joe, NeroRuleSet`).  The other `with()` flavors are
    // trivial.

    @Test public void testWith_Script_noInputs() {
        test("testWith_noInputs");
        var script = """
            define A/x;
            A(1);
            """;
        check(nero.toNeroScript(nero.withScript(script).debug().infer())).eq("""
            define A/x;
            A(1);
            """);
    }

    @Test public void testWith_Script_update() {
        test("testWith_update");
        var db = new FactSet();
        db.add(new Fact("A", List.of("x"), List.of(1.0)));
        var script = """
            define B/x;
            B(2);
            """;
        check(nero.toNeroScript(nero.withScript(script).debug().update(db))).eq("""
            define B/x;
            B(2);
            """);
        check(nero.toNeroScript(db)).eq("""
            define A/x;
            A(1);
            
            define B/x;
            B(2);
            """);
    }

    @Test public void testWith_Script_query_factset() {
        test("testWith_query_factset");
        var db = new FactSet();
        db.add(new Fact("A", List.of("x"), List.of(1.0)));
        var script = """
            define B/x;
            B(2);
            """;
        check(nero.toNeroScript(nero.withScript(script).debug()
            .query(db))).eq("""
            define B/x;
            B(2);
            """);
        check(nero.toNeroScript(db)).eq("""
            define A/x;
            A(1);
            """);
    }

    @Test public void testWith_Script_query_collections() {
        test("testWith_Script_query_collections");
        var list = new ArrayList<Fact>();
        list.add(new Fact("A", List.of("a"), List.of(1.0)));
        var script = """
            define B/x;
            B(2);
            """;
        check(nero.toNeroScript(nero.withScript(script).debug().query(list))).eq("""
            define B/x;
            B(2);
            """);
    }

    @Test public void testWith_Script_infer_parm() {
        test("testWith_Script_infer_parm");
        var script = """
            define A/x;
            A(x) :- query(parm: x);
            """;
        var results = nero.withScript(script).queryParm("parm", 1.0).infer();
        check(nero.toNeroScript(results)).eq("""
            define A/x;
            A(1);
            """);
    }

    @Test public void testWith_Script_query_parm() {
        test("testWith_Script_query_parm");
        var script = """
            define A/x;
            A(x) :- query(parm: x);
            """;
        var inputs = new FactSet();
        var results = nero.withScript(script)
            .queryParm("parm", 1.0).query(inputs);
        check(nero.toNeroScript(results)).eq("""
            define A/x;
            A(1);
            """);
    }

    @Test public void testWith_Script_infer_parms() {
        test("testWith_Script_infer_parms");
        var script = """
            define A/x;
            A(x) :- query(parm: x);
            """;
        var results = nero.withScript(script)
            .queryParms(Map.of("parm", 1.0)).infer();
        check(nero.toNeroScript(results)).eq("""
            define A/x;
            A(1);
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
            
            define Thing/thing,color;
            Thing("hat", "black");
            """;
        var facts = nero.withScript(script).infer();
        check(nero.toNeroScript(facts)).eq("""
            define Person/name,age;
            Person("Joe", 90);
            
            define Place/...;
            Place(attire: "Stetson", name: "Texas");
            
            define Thing/thing,color;
            Thing("hat", "black");
            """);
    }

    //-------------------------------------------------------------------------
    // toNeroAxiom

    @Test
    public void testToNeroAxiom_unorderedFact() {
        test("testToNeroAxiom_unorderedFact");
        var fact = new Fact("Thing", Map.of("id", "car", "color", "red"));
        check(nero.toNeroAxiom(fact))
            .eq("Thing(color: \"red\", id: \"car\");");
    }

    @Test
    public void testToNeroAxiom_orderedFact() {
        test("testToNeroAxiom_orderedFact");
        var fact = new Fact("Thing",
            List.of("id", "color"), List.of("car", "red"));
        check(nero.toNeroAxiom(fact))
            .eq("Thing(\"car\", \"red\");");
    }

    //-------------------------------------------------------------------------
    // toNeroTerm

    @Test
    public void testToNeroTerm() {
        test("testToNeroTerm");
        check(nero.toNeroTerm(null)).eq("null");
        check(nero.toNeroTerm(true)).eq("true");
        check(nero.toNeroTerm(false)).eq("false");
        check(nero.toNeroTerm(5.0)).eq("5");
        check(nero.toNeroTerm(5.1)).eq("5.1");
        check(nero.toNeroTerm(new Keyword("id"))).eq("#id");
        check(nero.toNeroTerm("abc")).eq("\"abc\"");

        checkThrow(() -> nero.toNeroTerm(this))
            .containsString("Non-Nero term:");
    }


    //------------------------------------------------------------------------
    // Helpers

    private SourceBuffer buffer(String script) {
        return new SourceBuffer("*test*", script);
    }
}
