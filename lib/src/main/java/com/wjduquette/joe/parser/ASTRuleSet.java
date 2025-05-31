package com.wjduquette.joe.parser;

import com.wjduquette.joe.scanner.Token;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class contains the Abstract Syntax Tree (AST) types for Nero
 * rule sets.
 */
public record ASTRuleSet(List<ASTOrderedAtom> facts, List<ASTRule> rules) {
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
        ASTOrderedAtom head,
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

    public sealed interface ASTAtom permits ASTOrderedAtom, ASTNamedAtom {
        /**
         * The Atom's relation
         * @return The relation name
         */
        Token relation();

        /**
         * The atom's terms.  This is to be used for retrieving data about
         * the terms, i.e., variable names.
         * @return The terms
         */
        Collection<ASTTerm> getTerms();

        /**
         * Gets a list of the variable tokens used in the atom.
         * @return The list
         */
        default List<Token> getVariableTokens() {
            return getTerms().stream()
                .filter(t -> t instanceof ASTVariable)
                .map(ASTTerm::token)
                .toList();
        }

        /**
         * Gets a list of the variable names used in the item.
         * @return The list
         */
        default List<String> getVariableNames() {
            return getTerms().stream()
                .filter(t -> t instanceof ASTVariable)
                .map(t -> t.token().lexeme())
                .toList();
        }
    }

    public record ASTOrderedAtom(
        Token relation,
        List<ASTTerm> terms
    ) implements ASTAtom {
        @Override public Collection<ASTTerm> getTerms() { return terms(); }
        @Override public String toString() {
            return "ASTIndexedAtom(" + relation.lexeme() + "," + terms + ")";
        }
    }

    public record ASTNamedAtom(
        Token relation,
        Map<Token,ASTTerm> termMap
    ) implements ASTAtom {
        @Override public Collection<ASTTerm> getTerms() {
            return termMap.values();
        }

        @Override public String toString() {
            var termString = termMap.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));
            return "ASTNamedAtom(" + relation.lexeme() + ", [" + termString + "])";
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
