package com.wjduquette.joe.parser;

import com.wjduquette.joe.nero.Schema;
import com.wjduquette.joe.nero.Term;
import com.wjduquette.joe.nero.Variable;
import com.wjduquette.joe.scanner.Token;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class contains the Abstract Syntax Tree (AST) types for Nero
 * rule sets.
 * @param schema The schema for the rule set's relations
 * @param axioms The axioms read from the rule set
 * @param rules The rules read from the rule set
 */
public record ASTRuleSet(
    Schema schema,
    List<ASTAtom> axioms,
    List<ASTRule> rules
) {
    @Override
    public String toString() {
        var buff = new StringBuilder();
        buff.append("ASTRuleSet[\n");

        for (var fact : axioms) {
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
        Collection<Term> getTerms();

        /**
         * Gets a list of the variable names used in the item.
         * @return The list
         */
        default List<String> getVariableNames() {
            return getTerms().stream()
                .filter(t -> t instanceof Variable)
                .map(t -> ((Variable)t).name())
                .toList();
        }
    }

    public record ASTOrderedAtom(
        Token relation,
        List<Term> terms
    ) implements ASTAtom {
        @Override public Collection<Term> getTerms() { return terms(); }
        @Override public String toString() {
            return "ASTOrderedAtom(" + relation.lexeme() + "," + terms + ")";
        }
    }

    public record ASTNamedAtom(
        Token relation,
        Map<Token,Term> termMap
    ) implements ASTAtom {
        @Override public Collection<Term> getTerms() {
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
        Variable a,
        Token op,
        Term b)
    {
        @Override public String toString() {
            return "ASTConstraint(" + a + "," + op.lexeme() + "," + b + ")";
        }
    }
}
