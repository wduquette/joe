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
     * @param schema The schema
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

    /**
     * Whether the rule set is stratifiable or not.
     * @return true or false
     */
    public boolean isStratified() {
        return isStratified;
    }

    /**
     * A list of the relations in each stratum, from lowest to highest.
     * @return the list
     */
    public List<List<String>> strata() {
        return strata;
    }

    /**
     * The rule set's schema
     * @return the schema
     */
    public Schema schema() {
        return schema;
    }

    /**
     * The rule set's axioms
     * @return the axioms
     */
    public Set<Atom> axioms() {
        return Collections.unmodifiableSet(axioms);
    }

    /**
     * The rule set's rules
     * @return the rules
     */
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
