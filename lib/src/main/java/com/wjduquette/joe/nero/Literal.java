package com.wjduquette.joe.nero;

import java.util.List;
import java.util.stream.Collectors;

public record Literal(Token relation, List<LiteralTerm> terms) {
    public List<String> getVariableNames() {
        return terms.stream()
            .filter(t -> t instanceof LiteralTerm.VariableToken)
            .map(LiteralTerm::toString)
            .toList();
    }

    public Fact asFact() {
        var realTerms = terms.stream().map(LiteralTerm::asTerm).toList();
        return new Fact(relation.lexeme(), realTerms);
    }

    @Override public String toString() {
        var termString = terms.stream().map(LiteralTerm::toString)
            .collect(Collectors.joining(", "));
        return relation.lexeme() + "(" + termString + ")";
    }

    public sealed interface LiteralTerm
        permits LiteralTerm.ConstantToken, LiteralTerm.VariableToken
    {
        Term asTerm();

        record ConstantToken(Token value) implements LiteralTerm {
            @Override public Term asTerm() {
                return new Term.Constant(value.literal());
            }

            @Override public String toString() {
                return value.lexeme();
            }
        }

        record VariableToken(Token name) implements LiteralTerm {
            @Override public Term asTerm() {
                return new Term.Variable(name.lexeme());
            }

            @Override public String toString() {
                return name.lexeme();
            }
        }
    }
}
