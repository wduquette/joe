package com.wjduquette.joe.nero;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class contains the Nero Abstract Syntax Tree types.
 */
class NeroAST {
    private NeroAST() {} // not instantiable

    //-------------------------------------------------------------------------
    // Clauses

    public sealed interface HornClause permits FactClause, RuleClause {}

    public record FactClause(AtomItem item) implements HornClause {
        public Fact asFact() {
            return item.asFact();
        }

        @Override public String toString() {
            return item + ".";
        }
    }

    public record RuleClause(
        AtomItem head,
        List<AtomItem> body,
        List<ConstraintItem> constraints
    ) implements HornClause {
        public Rule asRule() {
            var realBody = body.stream()
                .map(AtomItem::asAtom).toList();
            var realConstraints = constraints.stream()
                .map(ConstraintItem::asConstraint).toList();
            return new Rule(head.asHead(), realBody, realConstraints);
        }

        @Override public String toString() {
            var bodyString = body.stream().map(AtomItem::toString)
                .collect(Collectors.joining(", "));
            var whereString = constraints.stream().map(ConstraintItem::toString)
                .collect(Collectors.joining(", "));
            return head + " :- " + bodyString +
                (constraints.isEmpty() ? "" : " where " + whereString)
                + ".";
        }
    }

    //-------------------------------------------------------------------------
    // Items

    public record AtomItem(
        Token relation,
        List<TermToken> terms,
        boolean negated
    ) {
        /**
         * Gets a list of the variable names used in the item.
         * @return The list
         */
        public List<String> getVariableNames() {
            return terms.stream()
                .filter(t -> t instanceof VariableToken)
                .map(TermToken::toString)
                .toList();
        }

        /**
         * Converts the item to a Fact.  It's an error of the item contains
         * any variables or the item's negated flag is set.
         * @return The fact.
         */
        public Fact asFact() {
            if (negated) {
                throw new IllegalStateException("Atom is negated; cannot be a fact.");
            }

            var values = new ArrayList<>();
            for (var t : terms) {
                if (t instanceof ConstantToken c) {
                    values.add(c.value().literal());
                } else {
                    throw new IllegalStateException(
                        "Atom contains a variable term; cannot be a fact.");
                }
            }

            return new Fact(relation.lexeme(), values);
        }

        /**
         * Converts the item to an Atom, verifying that it's a valid
         * head atom.
         * @return The atom
         */
        public Atom asHead() {
            if (negated) {
                throw new IllegalStateException(
                    "Atom is negated; cannot be a rule head.");
            }
            return asAtom();
        }

        /**
         * Converts the item to an Atom, including negation.
         * @return The atom
         */
        public Atom asAtom() {
            var realTerms = terms.stream().map(TermToken::asTerm).toList();
            return new Atom(relation.lexeme(), realTerms, negated);
        }

        @Override public String toString() {
            var termString = terms.stream().map(TermToken::toString)
                .collect(Collectors.joining(", "));
            return (negated ? "not " : "") +
                relation.lexeme() + "(" + termString + ")";
        }
    }

    /**
     * A constraint of the form "a OP b".
     * @param a A bound variable
     * @param op A comparison operator
     * @param b A bound variable or constant
     */
    public record ConstraintItem(
        VariableToken a,
        Token op,
        TermToken b)
    {
        public Constraint asConstraint() {
            var realOp = switch (op.type()) {
                case BANG_EQUAL -> Op.NE;
                case EQUAL_EQUAL -> Op.EQ;
                case GREATER -> Op.GT;
                case GREATER_EQUAL -> Op.GE;
                case LESS -> Op.LT;
                case LESS_EQUAL -> Op.LE;
                default -> throw new IllegalStateException(
                    "Unknown operator token: " + op);
            };
            return new Constraint(a.asTerm(), realOp, b.asTerm());
        }

        @Override public String toString() {
            return a + " " + op.lexeme() + " " + b;
        }
    }

    //-------------------------------------------------------------------------
    // Terms

    /**
     * A term in an atom, either a constant or a variable.
     */
    public sealed interface TermToken
        permits ConstantToken, VariableToken
    {
        /** Converts the token to a `Term` as used in the engine. */
        Term asTerm();
    }

    /**
     * A token representing a constant term: a keyword, string, or
     * integer.
     * @param value The value token
     */
    public record ConstantToken(Token value) implements TermToken {
        @Override public Term.Constant asTerm() {
            return new Term.Constant(value.literal());
        }

        @Override public String toString() {
            return value.lexeme();
        }
    }

    /**
     * A token representing a variable term: an identifier.
     * @param name The token token
     */
    public record VariableToken(Token name) implements TermToken {
        @Override public Term.Variable asTerm() {
            return new Term.Variable(name.lexeme());
        }

        @Override public String toString() {
            return name.lexeme();
        }
    }
}
