package com.wjduquette.joe.nero;

import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.Ted;
import org.junit.Test;

import java.util.List;

import static com.wjduquette.joe.checker.Checker.check;
import static com.wjduquette.joe.checker.Checker.checkThrow;

/**
 * Tests for the NewNero class.
 */
public class NewNeroTest extends Ted {
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
    // Helpers

    private SourceBuffer buffer(String script) {
        return new SourceBuffer("*test*", script);
    }
}
