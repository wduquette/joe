package com.wjduquette.joe.nero;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An IndexedAtom is a {@link Atom} consisting of a relation name and a
 * list of {@link Term Terms}, which may be variables, wildcards, or constants.
 * An IndexedAtom can match indexed {@link Fact Facts}.
 * @param relation The relation name
 * @param terms The terms
 */
public record OrderedAtom(String relation, List<Term> terms)
    implements Atom
{
    @Override public boolean requiresOrderedFields() { return true; }

    @Override public Bindings matches(Fact fact, Bindings given) {
        var bindings = new Bindings(given);

        if (!fact.relation().equals(relation)) {
            return null;
        }

        if (!fact.isOrdered()) {
            return null;
        }

        var n = terms().size();
        if (fact.getFields().size() != n) return null;

        for (var i = 0; i < terms().size(); i++) {
            var t = terms().get(i);
            var f = fact.getFields().get(i);

            switch (t) {
                case Variable v -> {
                    var bound = bindings.get(v);

                    if (bound == null) {
                        bindings.put(v, f);
                    } else if (!bound.equals(f)) {
                        return null;
                    }
                }
                case Constant c -> {
                    if (!f.equals(c.value())) return null;
                }
                case Wildcard ignored -> {}
            }
        }

        return bindings;
    }

    @Override public String toString() {
        var termString = terms.stream().map(Term::toString)
            .collect(Collectors.joining(", "));
        return relation + "(" + termString + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        OrderedAtom atom = (OrderedAtom) o;
        return relation.equals(atom.relation) && terms.equals(atom.terms);
    }

    @Override
    public int hashCode() {
        int result = relation.hashCode();
        result = 31 * result + terms.hashCode();
        return result;
    }
}
