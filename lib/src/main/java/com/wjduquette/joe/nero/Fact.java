package com.wjduquette.joe.nero;

import java.util.List;
import java.util.stream.Collectors;

public record Fact(String relation, List<Term> terms) {
    @Override public String toString() {
        var termString = terms.stream().map(Term::toString)
            .collect(Collectors.joining(", "));
        return relation + "(" + termString + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Fact fact = (Fact) o;
        return relation.equals(fact.relation) && terms.equals(fact.terms);
    }

    @Override
    public int hashCode() {
        int result = relation.hashCode();
        result = 31 * result + terms.hashCode();
        return result;
    }
}
