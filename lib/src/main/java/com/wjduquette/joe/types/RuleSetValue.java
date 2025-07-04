package com.wjduquette.joe.types;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.FactSet;
import com.wjduquette.joe.nero.RuleEngine;
import com.wjduquette.joe.nero.RuleSet;

import java.util.*;

/**
 * A RuleSetValue wraps up a Nero
 * {@link com.wjduquette.joe.nero.RuleSet} with the additional data
 * required to make use of it in Joe scripts.  RuleSetValues are created
 * by Joe's `ruleset` declaration.
 */
public class RuleSetValue {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final RuleSet ruleset;
    private final Map<String, Object> exports;
    private boolean debug = false;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a RuleSetValue.
     * @param ruleset The Nero rule set.
     * @param exports map from relation to export callable
     */
    public RuleSetValue(
        RuleSet ruleset,
        Map<String, Object> exports
    ) {
        this.ruleset = ruleset;
        this.exports = exports;
    }

    //-------------------------------------------------------------------------
    // API

    /**
     * Gets the underlying Nero rule set.
     * @return The rule set
     */
    public RuleSet ruleset() {
        return ruleset;
    }

    /**
     * Gets the map from exported relation to factory callable
     * for exported facts.
     * @return The map
     */
    public Map<String,Object> exports() {
        return Collections.unmodifiableMap(exports);
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
    public Set<Fact> infer(FactBase db) {
        var engine = new RuleEngine(ruleset, db);
        engine.infer();
        return engine.getInferredFacts();
    }

    /**
     * Infer facts from the rule set, exporting to domain values as needed.
     * @return The new (possibly exported) facts.
     */
    public SetValue inferAndExport(Joe joe) {
        var engine = new RuleEngine(ruleset, new FactSet());
        engine.infer();
        return withExports(joe, engine.getInferredFacts());
    }

    /**
     * Infer facts from the rule set and the set of input facts, exporting
     * to domain values as needed.
     * @param inputs The input facts
     * @return The new (possibly exported) facts.
     */
    public SetValue inferAndExport(Joe joe, Collection<?> inputs) {
        var factSet = toFactSet(joe, inputs);
        var engine = new RuleEngine(ruleset, factSet);
        engine.infer();
        return withExports(joe, engine.getInferredFacts());
    }

    /**
     * Infer facts from the rule set and the set of input facts, exporting
     * to domain values as needed.
     * @param db The input facts
     * @return The new (possibly exported) facts.
     */
    public SetValue inferAndExport(Joe joe, FactBase db) {
        var engine = new RuleEngine(ruleset, db);
        engine.infer();
        return withExports(joe, engine.getInferredFacts());
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

    private SetValue withExports(Joe joe, Set<Fact> facts) {
        var result = new SetValue();

        for (var fact : facts) {
            var creator = exports().get(fact.relation());

            if (creator != null) {
                result.add(joe.call(creator, fact.getFields().toArray()));
            } else {
                result.add(fact);
            }
        }

        return result;
    }
}
