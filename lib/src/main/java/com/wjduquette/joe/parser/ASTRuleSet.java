package com.wjduquette.joe.parser;

import com.wjduquette.joe.nero.*;

import java.util.List;

/**
 * This class contains the Abstract Syntax Tree (AST) types for Nero
 * rule sets.
 * @param schema The schema for the rule set's relations
 * @param axioms The axioms read from the rule set
 * @param rules The rules read from the rule set
 */
public record ASTRuleSet(
    Schema schema,
    List<Atom> axioms,
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
        Atom head,
        List<Atom> body,
        List<Atom> negations,
        List<Constraint> constraints
    ) {
        @Override public String toString() {
            return "ASTRule(head=" + head + ",body=" + body +
                ",neg=" + negations + ",where=" + constraints + ")";
        }
    }
}
