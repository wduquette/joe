package com.wjduquette.joe.nero;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An NamedAtom is a {@link Atom} consisting of a relation name and a
 * map of field names and {@link Term Terms}. A NamedAtom matches
 * {@link Fact} fields by name.
 */
public final class NamedAtom implements Atom {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final String relation;
    private final Map<String,Term> termMap = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    public NamedAtom(String relation, Map<String,Term> termMap) {
        this.relation = relation;
        this.termMap.putAll(termMap);
    }

    //-------------------------------------------------------------------------
    // Methods

    @Override
    public String relation() {
        return relation;
    }

    /**
     * Gets an unmodifiable term map.
     * @return The map
     */
    public Map<String,Term> termMap() {
        return Collections.unmodifiableMap(termMap);
    }

    @Override public Collection<Term> getAllTerms() {
        return termMap.values();
    }

    @Override public String toString() {
        var termString = termMap.entrySet().stream()
            .map(e -> e.getKey() + ": " + e.getValue())
            .sorted()
            .collect(Collectors.joining(", "));
        return relation + "(" + termString + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        NamedAtom namedAtom = (NamedAtom) o;
        return relation.equals(namedAtom.relation) && termMap.equals(namedAtom.termMap);
    }

    @Override
    public int hashCode() {
        int result = relation.hashCode();
        result = 31 * result + termMap.hashCode();
        return result;
    }
}
