package com.wjduquette.joe.nero.parser;

import com.wjduquette.joe.nero.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class contains the Nero Abstract Syntax Tree types.
 */
public class NeroAST {
    private NeroAST() {} // not instantiable

    //-------------------------------------------------------------------------
    // Clauses

    public sealed interface HornClause permits Axiom, RuleClause {}

    public record Axiom(AtomItem item) implements HornClause {
        public Fact asFact() {
            return item.asFact();
        }

        @Override public String toString() {
            return item + ";";
        }
    }

    public record RuleClause(
        AtomItem head,
        List<AtomItem> body,
        List<AtomItem> negations,
        List<ConstraintItem> constraints
    ) implements HornClause {
        public Rule asRule() {
            return new Rule(
                head.asHead(),
                body.stream().map(AtomItem::asAtom).toList(),
                negations.stream().map(AtomItem::asAtom).toList(),
                constraints.stream().map(ConstraintItem::asConstraint).toList());
        }

        @Override public String toString() {
            var bodyString = body.stream().map(AtomItem::toString)
                .collect(Collectors.joining(", "));
            var negString = "not " + negations.stream().map(AtomItem::toString)
                .collect(Collectors.joining(", not "));
            var whereString = constraints.stream().map(ConstraintItem::toString)
                .collect(Collectors.joining(", "));
            return head + " :- " + bodyString +
                (negations.isEmpty() ? "" : ", " + negString) +
                (constraints.isEmpty() ? "" : " where " + whereString)
                + ";";
        }
    }

    //-------------------------------------------------------------------------
    // Items

    public record AtomItem(
        Token relation,
        List<TermToken> terms
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
            var values = new ArrayList<>();
            for (var t : terms) {
                if (t instanceof ConstantToken c) {
                    values.add(c.token().literal());
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
            return asAtom();
        }

        /**
         * Converts the item to an Atom, including negation.
         * @return The atom
         */
        public Atom asAtom() {
            var realTerms = terms.stream().map(TermToken::asTerm).toList();
            return new Atom(relation.lexeme(), realTerms);
        }

        @Override public String toString() {
            var termString = terms.stream().map(TermToken::toString)
                .collect(Collectors.joining(", "));
            return relation.lexeme() + "(" + termString + ")";
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
                case BANG_EQUAL -> Constraint.Op.NE;
                case EQUAL_EQUAL -> Constraint.Op.EQ;
                case GREATER -> Constraint.Op.GT;
                case GREATER_EQUAL -> Constraint.Op.GE;
                case LESS -> Constraint.Op.LT;
                case LESS_EQUAL -> Constraint.Op.LE;
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
        permits ConstantToken, VariableToken, WildcardToken
    {
        Token token();
        /** Converts the token to a `Term` as used in the engine. */
        Term asTerm();
    }

    /**
     * A token representing a constant term: a keyword, string, or
     * integer.
     * @param token The value token
     */
    public record ConstantToken(Token token) implements TermToken {
        @Override public Constant asTerm() {
            return new Constant(token.literal());
        }

        @Override public String toString() {
            return token.lexeme();
        }
    }

    /**
     * A token representing a variable term: an identifier.
     * @param token The token token
     */
    public record VariableToken(Token token) implements TermToken {
        @Override public Variable asTerm() {
            return new Variable(token.lexeme());
        }

        @Override public String toString() {
            return token.lexeme();
        }
    }

    /**
     * A token representing a wildcard term: an identifier beginning
     * with an underscore
     * @param token The token token
     */
    public record WildcardToken(Token token) implements TermToken {
        @Override public Wildcard asTerm() {
            return new Wildcard(token.lexeme());
        }

        @Override public String toString() {
            return token.lexeme();
        }
    }
}
