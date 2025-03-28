package com.wjduquette.joe.nero;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A Nero program is made up of Horn clauses, not statements.  Internally,
 * we distinguish between facts and rules.
 */
public sealed interface Clause
    permits Clause.FactClause, Clause.RuleClause
{
    record FactClause(Literal literal) implements Clause {
        public Fact asFact() {
            return literal.asFact();
        }

        @Override public String toString() {
            return literal + ".";
        }
    }

    record RuleClause(Literal head, List<Literal> body) implements Clause {
        public Rule asRule() {
            var realBody = body.stream().map(Literal::asFact).toList();
            return new Rule(head.asFact(), realBody);
        }

        @Override public String toString() {
            var bodyString = body.stream().map(Literal::toString)
                .collect(Collectors.joining(", "));
            return head + " :- " + bodyString + ".";
        }
    }
}
