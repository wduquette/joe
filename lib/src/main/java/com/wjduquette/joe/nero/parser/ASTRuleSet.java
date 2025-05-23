package com.wjduquette.joe.nero.parser;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class contains the Nero Abstract Syntax Tree types.
 */
public record ASTRuleSet(List<ASTIndexedAtom> facts, List<ASTRule> rules) {
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
        ASTIndexedAtom head,
        List<ASTAtom> body,
        List<ASTAtom> negations,
        List<ASTConstraint> constraints
    ) {
        @Override public String toString() {
            return "ASTRule(head=" + head + ",body=" + body +
                ",neg=" + negations + ",where=" + constraints + ")";
        }
    }

    //-------------------------------------------------------------------------
    // Items

    public sealed interface ASTAtom permits ASTIndexedAtom, ASTNamedAtom {
        Token relation();
        List<String> getVariableNames();
    }

    public record ASTIndexedAtom(
        Token relation,
        List<ASTTerm> terms
    ) implements ASTAtom {
        @Override public String toString() {
            return "ASTIndexedAtom(" + relation.lexeme() + "," + terms + ")";
        }

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
    }

    public record ASTNamedAtom(
        Token relation,
        Map<Token,ASTTerm> terms
    ) implements ASTAtom {
        @Override public String toString() {
            var termString = terms.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));
            return "ASTNamedAtom(" + relation.lexeme() + ", [" + termString + "])";
        }

        /**
         * Gets a list of the variable names used in the item.
         * @return The list
         */
        public List<String> getVariableNames() {
            return terms.values().stream()
                .filter(t -> t instanceof ASTVariable)
                .map(t -> t.token().lexeme())
                .toList();
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
    }

    /**
     * A token representing a constant term: a keyword, string, or
     * integer.
     * @param token The value token
     */
    public record ASTConstant(Token token) implements ASTTerm {
        @Override public String toString() {
            return token.lexeme();
        }
    }

    /**
     * A token representing a variable term: an identifier.
     * @param token The token token
     */
    public record ASTVariable(Token token) implements ASTTerm {
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
        @Override public String toString() {
            return token.lexeme();
        }
    }
}
