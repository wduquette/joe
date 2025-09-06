package com.wjduquette.joe.nero;

import com.wjduquette.joe.*;
import com.wjduquette.joe.parser.Parser;

import java.util.ArrayList;

public class NewNero {
    private NewNero() {} // Static Class

    //-------------------------------------------------------------------------
    // Parsing

    /**
     * Parses the source, checking for syntax errors.
     * Returns the rule set on success.
     * @param source The source
     * @return The rule set
     * @throws SyntaxError on parse error.
     */
    public static NeroRuleSet parse(SourceBuffer source) {
        var traces = new ArrayList<Trace>();
        var parser = new Parser(source, (t, flag) -> traces.add(t));
        var ruleset = parser.parseNero();
        if (!traces.isEmpty()) {
            throw new SyntaxError("Error in Nero input.", traces, false);
        }
        return ruleset;
    }

    //-------------------------------------------------------------------------
    // Compilation

    /**
     * Compiles the source, checking for syntax errors and stratification.
     * Returns the rule set on success and throws an appropriate error
     * on error.
     * @param source The source
     * @return The engine, which has not yet be run.
     * @throws SyntaxError on parse error.
     * @throws JoeError on stratification error.
     */
    public static NeroRuleSet compile(SourceBuffer source) {
        var ruleSet = parse(source);
        if (!ruleSet.isStratified()) {
            throw new JoeError("Nero rule set cannot be stratified.");
        }
        return ruleSet;
    }


    //-------------------------------------------------------------------------
    // Execution pipeline

    public static Pipeline with(String script) {
        return with(new Joe(), script);
    }

    public static Pipeline with(SourceBuffer source) {
        return with(new Joe(), source);
    }

    public static Pipeline with(NeroRuleSet ruleSet) {
        return with(new Joe(), ruleSet);
    }

    public static Pipeline with(Joe joe, String script) {
        var source = new SourceBuffer("*nero*", script);
        return with(joe, source);
    }

    public static Pipeline with(Joe joe, SourceBuffer source) {
        var ruleset = NewNero.compile(source);
        return with(joe, ruleset);
    }

    public static Pipeline with(Joe joe, NeroRuleSet ruleSet) {
        return new Pipeline(joe, ruleSet);
    }

    public static class Pipeline {
        //---------------------------------------------------------------------
        // Instance Variables

        private final Joe joe;
        private final NeroRuleSet ruleset;
        private boolean debug = false;

        //---------------------------------------------------------------------
        // Constructor

        private Pipeline(Joe joe, NeroRuleSet ruleset) {
            this.joe = joe;
            this.ruleset = ruleset;
        }

        //---------------------------------------------------------------------
        // Pipeline methods

        public Pipeline debug(boolean flag) {
            this.debug = flag;
            return this;
        }

        public Pipeline debug() {
            return debug(true);
        }

        //---------------------------------------------------------------------
        // Execution methods

        /**
         * Infers and returns all facts from the rule set.
         * @return The facts
         */
        public FactSet infer() {
            var engine = new RuleEngine(joe, ruleset, new FactSet());
            engine.setDebug(debug);
            return engine.infer();
        }

        /**
         * Infers all known facts from the rule set and fact set, updating
         * the fact set in place.  Returns the inferred facts.  Make an
         * explicit copy of the fact set to retain it unchanged.
         * @param facts The input facts
         * @return The inferred facts.
         */
        public FactSet infer(FactSet facts) {
            var engine = new RuleEngine(joe, ruleset, facts);
            engine.setDebug(debug);
            var inferred = engine.infer();

            // NOTE: RuleEngine doesn't currently update the input facts in
            // place!
            var known = engine.getKnownFacts();
            facts.clear();
            facts.addAll(known);

            return inferred;
        }
    }
}
