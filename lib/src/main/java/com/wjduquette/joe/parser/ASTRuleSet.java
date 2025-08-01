package com.wjduquette.joe.parser;

import com.wjduquette.joe.nero.*;

import java.util.List;
import java.util.Set;

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
    Set<Rule> rules
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
}
