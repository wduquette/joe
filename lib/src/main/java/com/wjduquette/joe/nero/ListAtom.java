package com.wjduquette.joe.nero;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A ListAtom is a {@link Atom} consisting of a relation name and a
 * list of {@link Term Terms}.  A ListAtom matches fact fields by
 * position.
 */
public final class ListAtom implements Atom {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final String relation;
    private final List<Term> terms = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    public ListAtom(String relation, List<Term> terms) {
        this.relation = relation;
        this.terms.addAll(terms);
    }

    //-------------------------------------------------------------------------
    // Methods

    @Override
    public String relation() {
        return relation;
    }

    /**
     * Returns the ListAtom's terms.
     * @return the terms
     */
    public List<Term> terms() {
        return Collections.unmodifiableList(terms);
    }

    @Override public Collection<Term> getAllTerms() {
        return terms;
    }

    @Override public String toString() {
        var termString = terms.stream().map(Term::toString)
            .collect(Collectors.joining(", "));
        return relation + "(" + termString + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ListAtom atom = (ListAtom) o;
        return relation.equals(atom.relation) && terms.equals(atom.terms);
    }

    @Override
    public int hashCode() {
        int result = relation.hashCode();
        result = 31 * result + terms.hashCode();
        return result;
    }
}
