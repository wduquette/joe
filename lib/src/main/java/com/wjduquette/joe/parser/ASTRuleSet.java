package com.wjduquette.joe.parser;

import com.wjduquette.joe.nero.Constraint;
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
        List<Constraint> constraints
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
        String relation();

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
        String relation,
        List<Term> terms
    ) implements ASTAtom {
        @Override public Collection<Term> getTerms() { return terms(); }
        @Override public String toString() {
            return "ASTOrderedAtom(" + relation + "," + terms + ")";
        }
    }

    public record ASTNamedAtom(
        String relation,
        Map<Token,Term> termMap
    ) implements ASTAtom {
        @Override public Collection<Term> getTerms() {
            return termMap.values();
        }

        @Override public String toString() {
            var termString = termMap.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));
            return "ASTNamedAtom(" + relation() + ", [" + termString + "])";
        }
    }
}
