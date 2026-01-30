package com.wjduquette.joe.nero;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A ListAtom is a {@link Atom} consisting of a relation name and a
 * list of {@link Term Terms}.  A ListAtom matches fact fields by
 * position.
 * @param relation The relation name
 * @param terms The terms
 */
public record ListAtom(String relation, List<Term> terms)
    implements Atom
{
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
