package com.wjduquette.joe.nero;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A literal atom, as parsed.  The `negated` flag is true if this atom was
 * negated in the body of a rule and false otherwise.
 * @param relation The relation name as a token
 * @param terms The terms in the atom
 * @param negated true or false.
 */
public record Literal(Token relation, List<LiteralTerm> terms, boolean negated) {
    public List<String> getVariableNames() {
        return terms.stream()
            .filter(t -> t instanceof LiteralTerm.VariableToken)
            .map(LiteralTerm::toString)
            .toList();
    }

    public Atom asAtom() {
        var realTerms = terms.stream().map(LiteralTerm::asTerm).toList();
        return new Atom(relation.lexeme(), realTerms, negated);
    }

    public Atom asHead() {
        if (negated) {
            throw new IllegalStateException(
                "Atom is negated; cannot be a rule head.");
        }
        var realTerms = terms.stream().map(LiteralTerm::asTerm).toList();
        return new Atom(relation.lexeme(), realTerms, negated);
    }

    public Fact asFact() {
        if (negated) {
            throw new IllegalStateException("Atom is negated; cannot be a fact.");
        }

        var values = new ArrayList<>();

        for (var term : terms) {
            if (term instanceof LiteralTerm.ConstantToken c) {
                values.add(c.value.literal());
            } else {
                throw new IllegalStateException(
                    "Atom contains variables; cannot be a fact.");
            }
        }

        return new Fact(relation.lexeme(), values);
    }

    @Override public String toString() {
        var termString = terms.stream().map(LiteralTerm::toString)
            .collect(Collectors.joining(", "));
        return (negated ? "not " : "") +
            relation.lexeme() + "(" + termString + ")";
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
