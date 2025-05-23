package com.wjduquette.joe.nero;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * An Atom is a predicate consisting of a relation name and a list of
 * {@link Term Terms}, which may be variables, wildcards, or constants.
 * Atoms are used to express {@link Rule} head and body atoms.
 * @param relation The relation name
 * @param terms The terms
 */
public record NamedAtom(String relation, Map<String,Term> terms)
    implements BodyAtom
{
    @Override
    public Bindings matches(Fact fact, Bindings given) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override public String toString() {
        var termString = terms.entrySet().stream()
            .map(e -> e.getKey() + ": " + e.getValue())
            .collect(Collectors.joining(", "));
        return relation + "(" + termString + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        NamedAtom namedAtom = (NamedAtom) o;
        return relation.equals(namedAtom.relation) && terms.equals(namedAtom.terms);
    }

    @Override
    public int hashCode() {
        int result = relation.hashCode();
        result = 31 * result + terms.hashCode();
        return result;
    }
}
