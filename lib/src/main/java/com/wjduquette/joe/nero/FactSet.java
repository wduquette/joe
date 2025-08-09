package com.wjduquette.joe.nero;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A collection of Facts indexed by relation.
 */
@SuppressWarnings("unused")
public class FactSet {
    //-------------------------------------------------------------------------
    // Instance Variables

    // All facts in the database.
    private final Set<Fact> facts = new HashSet<>();

    // Facts by relation
    private final Map<String,Set<Fact>> index = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an empty FactSet
     */
    public FactSet() {
        // Nothing to do
    }

    /**
     * Creates a FactSet containing the collection of Facts.
     * @param facts The facts
     */
    public FactSet(Collection<Fact> facts) {
        addAll(facts);
    }

    /**
     * Creates a copy of an existing FactSet.
     * @param other The facts
     */
    public FactSet(FactSet other) {
        addAll(other);
    }

    //-------------------------------------------------------------------------
    // Internals

    private Set<Fact> indexSet(String relation) {
        return index.computeIfAbsent(relation, key -> new HashSet<>());
    }

    //-------------------------------------------------------------------------
    // Public API

    /**
     * Adds a fact to the database.  Returns true if the fact wasn't already
     * present, and false otherwise.
     * @param fact The fact.
     * @return true or false
     */
    public boolean add(Fact fact) {
        if (facts.add(fact)) {
            indexSet(fact.relation()).add(fact);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds all facts in the collection to the database.
     * @param collection The collection
     */
    public void addAll(Collection<Fact> collection) {
        // There are several ways one could do this; consider timing
        // to determine which is fastest.
        facts.addAll(collection);
        reindex();
    }

    /**
     * Adds the contents of another FactBase to the database
     * @param other The other FactBase
     */
    public void addAll(FactSet other) {
        facts.addAll(other.facts);
        for (var e : other.index.entrySet()) {
            indexSet(e.getKey()).addAll(e.getValue());
        }
    }

    /**
     * Clears all content from the database.
     */
    public void clear() {
        facts.clear();
        index.clear();
    }

    /**
     * Drops a relation from the database, removing the relation's facts.
     * @param relation The relation
     */
    public void drop(String relation) {
        facts.removeAll(indexSet(relation));
        index.remove(relation);
    }

    /**
     * Deletes a specific fact from the database
     * @param fact The fact
     */
    public void remove(Fact fact) {
        if (facts.remove(fact)) {
            indexSet(fact.relation()).remove(fact);
        }
    }

    /**
     * Deletes a collection of facts from the database.
     * @param collection The facts
     */
    public void removeAll(Collection<Fact> collection) {
        // There are several ways one could do this; consider timing
        // to determine which is fastest.
        facts.removeAll(collection);
        reindex();
    }

    /**
     * Deletes the facts in another FactBase from the database.
     * @param other The other FactBase
     */
    public void removeAll(FactSet other) {
        // There are a number of ways one could do this; consider
        // timing to find out.
        facts.removeAll(other.facts);
        reindex();
    }

    /**
     * Renames a relation, replacing any previous relation with the same name.
     * @param oldName The name of an existing relation
     * @param newName A new name for the existing relation.
     */
    public void rename(String oldName, String newName) {
        var newFacts = new HashSet<Fact>();
        for (var fact : indexSet(oldName)) {
            var newFact = switch (fact) {
                case ListFact f ->
                    new ListFact(newName, f.fields());
                case MapFact f ->
                    new MapFact(newName, f.fieldMap());
                case PairFact f ->
                    new PairFact(newName, f.getFieldNames(), f.getFieldMap());
            };
            newFacts.add(newFact);
        }

        drop(oldName);
        drop(newName);
        facts.addAll(newFacts);
        index.put(newName, newFacts);
    }

    /**
     * Gets a read-only set of all facts in the database.
     * @return The set
     */
    public Set<Fact> getAll() {
        return Collections.unmodifiableSet(facts);
    }

    /**
     * Gets the set of relations of the facts in this FactSet
     * @return The set
     */
    public Set<String> getRelations() {
        // The index contains a relation set for each relation that has been
        // requested, whether there are any facts or not.  Skip those.
        return index.keySet().stream()
            .filter(k -> !index.get(k).isEmpty())
            .collect(Collectors.toSet());
    }

    /**
     * Gets a read-only set of all facts in the database that have
     * the given relation.
     * @return The set
     */
    public Set<Fact> getRelation(String relation) {
        return Collections.unmodifiableSet(indexSet(relation));
    }

    /**
     * Is the database empty?
     * @return true or false
     */
    public boolean isEmpty() {
        return facts.isEmpty();
    }

    /**
     * Returns the number of facts in the database.
     * @return the count
     */
    public int size() {
        return facts.size();
    }

    // Re-indexes the set of facts, e.g., after major deletions.
    private void reindex() {
        index.clear();
        for (var fact : facts) {
            indexSet(fact.relation()).add(fact);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        FactSet factSet = (FactSet) o;
        return facts.equals(factSet.facts);
    }

    @Override
    public int hashCode() {
        return facts.hashCode();
    }
}
