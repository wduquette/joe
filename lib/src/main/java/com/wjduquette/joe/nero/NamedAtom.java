package com.wjduquette.joe.nero;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * An NamedAtom is a {@link Atom} consisting of a relation name and a
 * map of field names and {@link Term Terms}, which may be variables,
 * wildcards, or constants. A NamedAtom can match any {@link Fact},
 * identifying fields by name.
 * @param relation The relation name
 * @param terms The terms
 */
public record NamedAtom(String relation, Map<String,Term> terms)
    implements Atom
{
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
