package com.wjduquette.joe.nero;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A ListAtom is a {@link Atom} consisting of a relation name and a
 * list of {@link Term Terms}.  A ListAtom matches fact fields by
 * position.
 */
public final class ListAtom extends Atom {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final List<Term> terms = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a normal ListAtom with the given inputs.
     * @param relation The relation name
     * @param terms The list of terms.
     */
    public ListAtom(String relation, List<Term> terms) {
        this(false, relation, terms);
    }

    /**
     * Creates a possibly-negated ListAtom with the given inputs.
     * @param negated true or false
     * @param relation The relation name
     * @param terms The list of terms.
     */
    public ListAtom(boolean negated, String relation, List<Term> terms) {
        super(negated, relation);
        this.terms.addAll(terms);
    }

    //-------------------------------------------------------------------------
    // Methods

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
        return relation() + "(" + termString + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ListAtom atom = (ListAtom) o;
        return super.equals(atom) && terms.equals(atom.terms);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + terms.hashCode();
        return result;
    }
}
