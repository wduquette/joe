package com.wjduquette.joe.nero;

import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.parser.ASTRuleSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Compiles a Nero {@link RuleSet} from an
 * {@link com.wjduquette.joe.parser.ASTRuleSet} given its configuration.
 */
public class RuleSetCompiler {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The AST
    private final ASTRuleSet ast;

    //-------------------------------------------------------------------------
    // Constructor

    public RuleSetCompiler(ASTRuleSet ast) {
        this.ast = ast;
    }

    //-------------------------------------------------------------------------
    // Public API

    /**
     * Compiles the Nero source, producing a rule set.
     * @return The RuleSet
     * @throws JoeError on compilation failure.
     */
    public RuleSet compile() {
        return new RuleSet(ast.schema(), ast.axioms(), ast.rules());
    }
}
