package com.wjduquette.joe.nero;

import java.util.*;

/**
 * A collection of axioms and rules, ready for processing by the Nero engine.
 */
public class NeroRuleSet {
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
    public NeroRuleSet(Schema schema, Set<Atom> axioms, Set<Rule> rules) {
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

    /**
     * Gets a set of the names of all relations used in the rule set.
     * @return The set
     */
    public Set<String> getRelations() {
        var result = new HashSet<String>();
        axioms.forEach(a -> result.add(a.relation()));
        rules.forEach(r -> result.add(r.head().relation()));
        return result;
    }
}
