package com.wjduquette.joe.nero;

import java.util.HashSet;
import java.util.Set;

/**
 * A collection of facts and rules, ready for processing by the Nero engine.
 */
public class RuleSet {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Set<Fact> facts = new HashSet<>();
    private final Set<Rule> rules = new HashSet<>();

    //-------------------------------------------------------------------------
    // Constructor

    public RuleSet() {
        // nothing to do
    }

    public boolean add(Fact fact) {
        return facts.add(fact);
    }

    public boolean add(Rule rule) {
        return rules.add(rule);
    }

    public Set<Fact> getFacts() {
        return facts;
    }

    public Set<Rule> getRules() {
        return rules;
    }
}
