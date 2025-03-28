package com.wjduquette.joe.nero;

import java.util.List;
import java.util.stream.Collectors;

public record Literal(Token relation, List<Term> terms) {
    @Override public String toString() {
        var termString = terms.stream().map(Term::toString)
            .collect(Collectors.joining(", "));
        return relation.lexeme() + "(" + termString + ")";
    }

    public sealed interface Term
        permits Term.Constant, Term.Variable
    {
        record Constant(Token value) implements Term {
            @Override public String toString() {
                return value.lexeme();
            }
        }

        record Variable(Token name) implements Term {
            @Override public String toString() {
                return name.lexeme();
            }
        }
    }
}
