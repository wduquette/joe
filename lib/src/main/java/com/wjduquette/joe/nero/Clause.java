package com.wjduquette.joe.nero;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A Nero program is made up of Horn clauses, not statements.  Internally,
 * we distinguish between facts and rules.
 */
public sealed interface Clause
    permits Clause.Fact, Clause.Rule
{
    record Fact(Literal literal) implements Clause {
        @Override public String toString() {
            return literal + ".";
        }
    }

    record Rule(Literal head, List<Literal> body) implements Clause {
        @Override public String toString() {
            var bodyString = body.stream().map(Literal::toString)
                .collect(Collectors.joining(", "));
            return head + " :- " + bodyString + ".";
        }
    }
}
