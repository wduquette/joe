package com.wjduquette.joe.types;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.FactSet;
import com.wjduquette.joe.nero.RuleEngine;
import com.wjduquette.joe.nero.NeroRuleSet;

import java.util.*;

/**
 * A RuleSetValue wraps up a Nero
 * {@link NeroRuleSet} with the additional data
 * required to make use of it in Joe scripts.  RuleSetValues are created
 * by Joe's `ruleset` declaration.
 */
public class RuleSetValue {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final NeroRuleSet ruleset;
    private boolean debug = false;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a RuleSetValue.
     * @param ruleset The Nero rule set.
     */
    public RuleSetValue(NeroRuleSet ruleset) {
        this.ruleset = ruleset;
    }

    //-------------------------------------------------------------------------
    // API

    /**
     * Gets the underlying Nero rule set.
     * @return The rule set
     */
    public NeroRuleSet ruleset() {
        return ruleset;
    }

    /**
     * Gets whether the debug flag has been set or not.
     * @return The flag
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Sets the debug flag.  If enabled, the Nero engine will output
     * a detailed trace of its activities.
     * @param debug The flag
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Infer facts from the rule set and the database of facts.
     * @return The new facts.
     */
    public Set<Fact> infer() {
        var engine = new RuleEngine(ruleset);
        engine.infer();
        return engine.getInferredFacts();
    }

    /**
     * Infer facts from the rule set and the database of facts.
     * @return The new facts.
     */
    public Set<Fact> infer(FactBase db) {
        var engine = new RuleEngine(ruleset, db);
        engine.infer();
        return engine.getInferredFacts();
    }

    /**
     * Infer facts from the rule set and the set of input facts.
     * @param inputs The input facts
     * @return The new (possibly exported) facts.
     */
    public Set<Fact> infer(Joe joe, Collection<?> inputs) {
        var factSet = toFactSet(joe, inputs);
        var engine = new RuleEngine(ruleset, factSet);
        engine.infer();
        return engine.getInferredFacts();
    }

    /**
     * Gets whether the rule set is stratified or not.
     * @return true or false.
     */
    public boolean isStratified() {
        return ruleset.isStratified();
    }

    private FactSet toFactSet(Joe joe, Collection<?> inputs) {
        // FIRST, Build the list of input facts, wrapping values of proxied
        // types as TypedValues so that they can be used as Facts
        // by Nero.
        var factSet = new FactSet();

        for (var input : inputs) {
            // Throws JoeError if the input cannot be converted to a Fact
            factSet.add(joe.toFact(input));
        }

        return factSet;
    }
}
