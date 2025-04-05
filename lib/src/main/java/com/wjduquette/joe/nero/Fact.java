package com.wjduquette.joe.nero;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A Fact is a ground fact whose terms are Java values.
 * This is a temporary type; Facts will ultimately be JoeValues.
 * @param relation The relation
 * @param terms The values.
 */
public record Fact(String relation, List<Object> terms) {
    @Override public String toString() {
        var termString = terms.stream().map(Object::toString)
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
