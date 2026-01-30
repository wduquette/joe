package com.wjduquette.joe.nero;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A Nero atom, i.e., a relation with its terms.
 */
public sealed interface Atom permits
    MapAtom, ListAtom
{
    /**
     * Gets the atom's relation string.
     * @return The relation
     */
    String relation();

    /**
     * Gets all the terms in the atom.
     * @return The terms.
     */
    Collection<Term> getAllTerms();

    /**
     * Gets the names of the atom's variables.
     * @return the names
     */
    default Set<String> getVariableNames() {
        var set = new HashSet<String>();
        for (var t : getAllTerms()) set.addAll(Term.getVariableNames(t));
        return set;
    }
}
