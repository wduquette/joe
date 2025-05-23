package com.wjduquette.joe.nero;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An Atom is a predicate consisting of a relation name and a list of
 * {@link Term Terms}, which may be variables, wildcards, or constants.
 * Atoms are used to express {@link Rule} head and body atoms.
 * @param relation The relation name
 * @param terms The terms
 */
public record HeadAtom(String relation, List<Term> terms) {
    @Override public String toString() {
        var termString = terms.stream().map(Term::toString)
            .collect(Collectors.joining(", "));
        return relation + "(" + termString + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        HeadAtom atom = (HeadAtom) o;
        return relation.equals(atom.relation) && terms.equals(atom.terms);
    }

    @Override
    public int hashCode() {
        int result = relation.hashCode();
        result = 31 * result + terms.hashCode();
        return result;
    }
}
