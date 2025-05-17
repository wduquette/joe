package com.wjduquette.joe.nero.parser;

import com.wjduquette.joe.nero.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the Nero Abstract Syntax Tree types.
 */
public record ASTRuleSet(List<ASTAtom> facts, List<ASTRule> rules) {
    @Override
    public String toString() {
        var buff = new StringBuilder();
        buff.append("ASTRuleSet[\n");

        for (var fact : facts) {
            buff.append("  Fact: ").append(fact).append("\n");
        }

        for (var rule : rules) {
            buff.append("  Rule: ").append(rule).append("\n");
        }

        buff.append("]");

        return buff.toString();
    }

    //-------------------------------------------------------------------------
    // Clauses

    public record ASTRule(
        ASTAtom head,
        List<ASTAtom> body,
        List<ASTAtom> negations,
        List<ASTConstraint> constraints
    ) {
        public Rule asRule() {
            return new Rule(
                head.asHead(),
                body.stream().map(ASTAtom::asAtom).toList(),
                negations.stream().map(ASTAtom::asAtom).toList(),
                constraints.stream().map(ASTConstraint::asConstraint).toList());
        }

        @Override public String toString() {
            return "ASTRule(head=" + head + ",body=" + body +
                ",neg=" + negations + ",where=" + constraints + ")";
        }
    }

    //-------------------------------------------------------------------------
    // Items

    public record ASTAtom(
        Token relation,
        List<ASTTerm> terms
    ) {
        /**
         * Gets a list of the variable names used in the item.
         * @return The list
         */
        public List<String> getVariableNames() {
            return terms.stream()
                .filter(t -> t instanceof ASTVariable)
                .map(t -> t.token().lexeme())
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
                if (t instanceof ASTConstant c) {
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
            var realTerms = terms.stream().map(ASTTerm::asTerm).toList();
            return new Atom(relation.lexeme(), realTerms);
        }

        @Override public String toString() {
            return "ASTAtom(" + relation.lexeme() + "," + terms + ")";
        }
    }

    /**
     * A constraint of the form "a OP b".
     * @param a A bound variable
     * @param op A comparison operator
     * @param b A bound variable or constant
     */
    public record ASTConstraint(
        ASTVariable a,
        Token op,
        ASTTerm b)
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
            return "ASTConstraint(" + a + "," + op.lexeme() + "," + b + ")";
        }
    }

    //-------------------------------------------------------------------------
    // Terms

    /**
     * A term in an atom, either a constant or a variable.
     */
    public sealed interface ASTTerm
        permits ASTConstant, ASTVariable, ASTWildcard
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
    public record ASTConstant(Token token) implements ASTTerm {
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
    public record ASTVariable(Token token) implements ASTTerm {
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
    public record ASTWildcard(Token token) implements ASTTerm {
        @Override public Wildcard asTerm() {
            return new Wildcard(token.lexeme());
        }

        @Override public String toString() {
            return token.lexeme();
        }
    }
}
