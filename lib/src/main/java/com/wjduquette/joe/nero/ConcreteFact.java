package com.wjduquette.joe.nero;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The default concrete Fact type for use with {@link Nero}.
 * @param relation The relation
 * @param terms The values.
 */
public record ConcreteFact(String relation, List<Object> terms)
    implements Fact
{
    @Override public String toString() {
        var termString = terms.stream().map(Object::toString)
            .collect(Collectors.joining(", "));
        return relation + "(" + termString + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ConcreteFact fact = (ConcreteFact) o;
        return relation.equals(fact.relation) && terms.equals(fact.terms);
    }

    @Override
    public int hashCode() {
        int result = relation.hashCode();
        result = 31 * result + terms.hashCode();
        return result;
    }
}
