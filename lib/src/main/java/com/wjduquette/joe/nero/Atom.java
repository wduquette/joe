package com.wjduquette.joe.nero;

import java.util.List;
import java.util.stream.Collectors;

public record Atom(String relation, List<Term> terms, boolean negated) {
    @Override public String toString() {
        var termString = terms.stream().map(Term::toString)
            .collect(Collectors.joining(", "));
        return (negated ? "not " : "" ) + relation + "(" + termString + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Atom atom = (Atom) o;
        return negated == atom.negated
            && relation.equals(atom.relation)
            && terms.equals(atom.terms);
    }

    @Override
    public int hashCode() {
        int result = relation.hashCode();
        result = 31 * result + terms.hashCode();
        result = 31 * result + Boolean.hashCode(negated);
        return result;
    }
}
