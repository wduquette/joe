package com.wjduquette.joe.nero;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A MapAtom is a {@link Atom} consisting of a relation name and a
 * map of field names and {@link Term Terms}. A MapAtom matches
 * {@link Fact} fields by name.
 */
public final class MapAtom extends Atom {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Map<String,Term> termMap = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a normal MapAtom with the given inputs.
     * @param relation The relation name
     * @param termMap The map of field names and terms
     */
    public MapAtom(String relation, Map<String,Term> termMap) {
        this(false, relation, termMap);
    }

    /**
     * Creates a possibly-negated MapAtom with the given inputs.
     * @param negated true or false
     * @param relation The relation name
     * @param termMap The map of field names and terms
     */
    public MapAtom(boolean negated, String relation, Map<String,Term> termMap) {
        super(negated, relation);
        this.termMap.putAll(termMap);
    }

    //-------------------------------------------------------------------------
    // Methods

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
        return relation() + "(" + termString + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        MapAtom mapAtom = (MapAtom) o;
        return super.equals(mapAtom) && termMap.equals(mapAtom.termMap);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + termMap.hashCode();
        return result;
    }
}
