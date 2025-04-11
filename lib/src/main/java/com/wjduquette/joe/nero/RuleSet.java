package com.wjduquette.joe.nero;

import java.util.*;

/**
 * A RuleSet captures all input from a Nero program: the rules and
 * the base facts.  It can compute whether the rule set is stratified
 * or not.
 */
public class RuleSet {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The rule set's rules.
    private final List<Rule> rules = new ArrayList<>();

    // The rule set's base facts.
    private final List<Fact> baseFacts = new ArrayList<>();

    // Head relations by stratum.
    private boolean strataPending = false;
    private List<List<String>> strata = null;


    //-------------------------------------------------------------------------
    // Constructor

    public RuleSet(List<Rule> rules, List<Fact> baseFacts) {
        // FIRST, analyze the rule set
        var graph = new DependencyGraph(rules);
        this.strata = graph.strata();

        // NEXT, save the base facts.
        this.baseFacts.addAll(baseFacts);
    }

    //-------------------------------------------------------------------------
    // Public API

    /**
     * Gets the rules defined by the rule set.
     * @return The rules.
     */
    public List<Rule> getRules() {
        return rules;
    }

    /**
     * Gets the base facts defined by the rule set.
     * @return The base facts.
     */
    public List<Fact> getBaseFacts() {
        return baseFacts;
    }

    /**
     * Gets whether the rule set is stratified or not.
     * @return true or false.
     */
    public boolean isStratified() {
        stratify();
        return strata != null;
    }

    /**
     * If the rule set is stratified, returns the list of rule predicates
     * in each stratum.
     * @return The list
     * @throws IllegalStateException if the rule set is not stratified.
     */
    public List<List<String>> strata() {
        stratify();
        if (strata == null) {
            throw new IllegalStateException("Rule set is not stratified.");
        }
        return strata;
    }

    private void stratify() {
        if (strataPending) {
            var graph = new DependencyGraph(rules);
            this.strata = graph.strata();
            strataPending = false;
        }
    }
}
