package com.wjduquette.joe.nero;

import java.util.*;

/**
 * A fact base, including any number of facts of various types.  A fact
 * base is essentially a Set that supports easy access to the facts of a
 * particular type.
 */
public class FactBase {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The set of all facts.
    private final Set<Fact> knownFacts = new HashSet<>();

    // Facts by relation
    private final Map<String, List<Fact>> factMap = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    public FactBase(List<Fact> baseFacts) {
        // NEXT, save the base facts.
        baseFacts.forEach(this::add);
    }

    //-------------------------------------------------------------------------
    // API

    /**
     * Adds all facts in the list, return true if any were new.
     * @param facts The facts.
     * @return true false
     */
    public boolean addAll(List<Fact> facts) {
        var size = knownFacts.size();
        facts.forEach(this::add);
        return knownFacts.size() > size;
    }

    /**
     * Adds the fact into the set of known facts.  Returns true if the
     * fact was previously unknown, and false otherwise.
     * Eventually this will be public, but we'll need to extend the
     * data model.
     * @param fact The fact
     * @return true or false
     */
    public boolean add(Fact fact) {
        if (knownFacts.add(fact)) {
            var list = getFacts(fact.relation());
            list.add(fact);

            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets all facts for the given relation.
     * @param relation The relation name
     * @return The list of facts.
     */
    public List<Fact> getFacts(String relation) {
        return factMap.computeIfAbsent(relation,
            key -> new ArrayList<>());
    }


    /**
     * Gets the set all known facts.
     * @return The set
     */
    public Set<Fact> getFacts() {
        return Collections.unmodifiableSet(knownFacts);
    }
}
