package com.wjduquette.joe.parser;

import com.wjduquette.joe.patterns.Pattern;
import com.wjduquette.joe.scanner.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link com.wjduquette.joe.patterns.Pattern}, as included in the parser's
 * Abstract Syntax Tree.
 */
public class ASTPattern {
    //-------------------------------------------------------------------------
    // Instance Variables

    private Pattern pattern = null;
    private final List<Expr> exprs = new ArrayList<>();
    private final List<Token> bindings = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the AST object.
     */
    public ASTPattern() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // Parser API

    /**
     * Sets the complete pattern once parsing is complete.
     * @param pattern The pattern
     */
    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Adds an interpolated variable constant to the constants list, and
     * returns the {@link Pattern.Expression} for
     * inclusion in the larger pattern.
     * @param varName A variable name token.
     * @return The pattern
     */
    public Pattern.Expression addVarExpr(Token varName) {
        return addExpr(new Expr.VarGet(varName));
    }

    /**
     * Adds an interpolated expression constant to the constants list, and
     * returns the {@link Pattern.Expression} for
     * inclusion in the larger pattern.
     * @param expr The expression
     * @return The pattern
     */
    public Pattern.Expression addExpr(Expr expr) {
        int index = exprs.size();
        exprs.add(expr);
        return new Pattern.Expression(index);
    }

    /**
     * Adds the binding name to the bindings list.
     * @param varName The variable name
     */
    public void saveBinding(Token varName) {
        bindings.add(varName);
    }

    //-------------------------------------------------------------------------
    // Resolver/Interpreter API

    /**
     * Get the wrapped {@link com.wjduquette.joe.patterns.Pattern}.
     * @return The pattern
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Gets a list of constant expressions corresponding to the
     * constant IDs in the pattern.
     * @return The list
     */
    public List<Expr> getExprs() {
        return exprs;
    }

    /**
     * Gets a list of variable names corresponding to the binding IDs
     * in the pattern.
     * @return The list
     */
    public List<Token> getVariableTokens() {
        return bindings;
    }
}
