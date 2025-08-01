package com.wjduquette.joe.nero;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A collection of axioms and rules, ready for processing by the Nero engine.
 */
public class RuleSet {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The Schema
    private final Schema schema;

    // The axioms and rules
    private final Set<Atom> axioms;
    private final Set<Rule> rules;

    // Rule Head relations by stratum.
    private final boolean isStratified;
    private final List<List<String>> strata;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new rule set given the schema and the sets of axioms and
     * rules.  Transfers ownership of the two sets to the new instance, and
     * computes the stratification of the rules.
     * @param axioms The axiomatic facts
     * @param rules The rules
     */
    public RuleSet(Schema schema, Set<Atom> axioms, Set<Rule> rules) {
        this.schema = schema;
        this.axioms = axioms;
        this.rules = rules;

        var stratifier = new Stratifier(rules);
        this.isStratified = stratifier.isStratified();

        this.strata = isStratified ? stratifier.strata() : null;
    }

    //-------------------------------------------------------------------------
    // Public API

    public boolean isStratified() {
        return isStratified;
    }

    public List<List<String>> strata() {
        return strata;
    }

    public Schema schema() {
        return schema;
    }

    public Set<Atom> axioms() {
        return Collections.unmodifiableSet(axioms);
    }

    public Set<Rule> rules() {
        return Collections.unmodifiableSet(rules);
    }
}
