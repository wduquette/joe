package com.wjduquette.joe.nero;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A Nero atom, i.e., a relation with its terms.
 */
public sealed abstract class Atom permits MapAtom, ListAtom {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final boolean negated;
    private final String relation;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Initializes the atom's shared properties.
     * @param negated true or false
     * @param relation the name
     */
    protected Atom(boolean negated, String relation) {
        this.negated = negated;
        this.relation = relation;
    }

    //-------------------------------------------------------------------------
    // Instance Methods

    /**
     * Whether the atom is negated or not.  Head atoms are never negated.
     * @return true or false
     */
    public final boolean isNegated() {
        return negated;
    }

    /**
     * Gets the atom's relation string.
     * @return The relation
     */
    public final String relation() {
        return relation;
    }

    /**
     * Gets the names of the atom's variables.
     * @return the names
     */
    public final Set<String> getVariableNames() {
        var set = new HashSet<String>();
        for (var t : getAllTerms()) set.addAll(Term.getVariableNames(t));
        return set;
    }

    //-------------------------------------------------------------------------
    // Abstract API

    /**
     * Gets all the terms in the atom.
     * @return The terms.
     */
    abstract public Collection<Term> getAllTerms();

    //-------------------------------------------------------------------------
    // equals and hashCode


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Atom atom)) return false;
        return relation.equals(atom.relation);
    }

    @Override
    public int hashCode() {
        return relation.hashCode();
    }
}
