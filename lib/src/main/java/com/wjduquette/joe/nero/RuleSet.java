package com.wjduquette.joe.nero;

import java.util.*;

/**
 * A Nero rule base, including all facts and rules read from Nero input.
 * For now, we only consider these; later, we'll be able to add in facts
 * from outside, and merge rule sets.
 */
public class RuleSet {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final List<Rule> rules = new ArrayList<>();

    // Facts as read from the Nero program.
    private final List<Fact> baseFacts = new ArrayList<>();

    // The current set of known facts.
    private final Set<Fact> knownFacts = new HashSet<>();

    // Facts by relation
    private final Map<String, List<Fact>> factMap = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    public RuleSet(List<Rule> rules, List<Fact> baseFacts) {
        this.rules.addAll(rules);
        this.baseFacts.addAll(baseFacts);
        baseFacts.forEach(this::addFact);
    }

    //-------------------------------------------------------------------------
    // API

    /**
     * Adds the fact into the set of known facts.  Returns true if the
     * fact was previously unknown, and false otherwise.
     * @param fact The fact
     * @return true or false
     */
    public boolean addFact(Fact fact) {
        if (!knownFacts.contains(fact)) {
            var list = factMap.computeIfAbsent(fact.relation(),
                key -> new ArrayList<>());
            list.add(fact);

            knownFacts.add(fact);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Executes the inference algorithm, computing all facts knowable
     * from the rules.
     */
    public void ponder() {

    }

    public Set<Fact> getKnownFacts() {
        return Collections.unmodifiableSet(knownFacts);
    }
}
