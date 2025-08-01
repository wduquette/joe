package com.wjduquette.joe.nero;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Nero atom, i.e., a relation with its terms.
 */
public sealed interface Atom permits
    NamedAtom, OrderedAtom
{
    /**
     * Gets the atom's relation string.
     * @return The relation
     */
    String relation();

    // Gets all terms belonging to the atom.
    Collection<Term> getAllTerms();

    // Gets the names of the atom's variables.
    default Set<String> getVariableNames() {
        return getAllTerms().stream()
            .filter(t -> t instanceof Variable)
            .map(v -> ((Variable)v).name())
            .collect(Collectors.toSet());
    }
}
