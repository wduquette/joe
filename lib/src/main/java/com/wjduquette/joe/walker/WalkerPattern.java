package com.wjduquette.joe.walker;

import com.wjduquette.joe.patterns.Pattern;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link com.wjduquette.joe.patterns.Pattern}, plus related data needed
 * by the {@link Resolver} and {@link Interpreter}.
 */
class WalkerPattern {
    //-------------------------------------------------------------------------
    // Instance Variables

    private Pattern pattern = null;
    private final List<Expr> constants = new ArrayList<>();
    private final List<Token> bindings = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    public WalkerPattern() {
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
     * Adds a simple literal constant to the constants list, and returns
     * the {@link com.wjduquette.joe.patterns.Pattern.Constant} for
     * inclusion in the larger pattern.
     * @param value A double, string, boolean, keyword, or null.
     * @return The pattern
     */
    public Pattern.Constant addLiteralConstant(Object value) {
        return addExprConstant(new Expr.Literal(value));
    }

    /**
     * Adds an interpolated variable constant to the constants list, and
     * returns the {@link com.wjduquette.joe.patterns.Pattern.Constant} for
     * inclusion in the larger pattern.
     * @param varName A variable name token.
     * @return The pattern
     */
    public Pattern.Constant addVarConstant(Token varName) {
        return addExprConstant(new Expr.Variable(varName));
    }

    /**
     * Adds an interpolated expression constant to the constants list, and
     * returns the {@link com.wjduquette.joe.patterns.Pattern.Constant} for
     * inclusion in the larger pattern.
     * @param expr The expression
     * @return The pattern
     */
    public Pattern.Constant addExprConstant(Expr expr) {
        int index = constants.size();
        constants.add(expr);
        return new Pattern.Constant(index);
    }

    /**
     * Given a binding variable name, adds it to the bindings list and
     * returns the binding ID.  Use this for binding IDs included in
     * other patterns.
     * @param varName The variable name
     * @return The ID
     */
    public int getBindingID(Token varName) {
        int index = bindings.size();
        bindings.add(varName);
        return index;
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
    public List<Expr> getConstants() {
        return constants;
    }

    /**
     * Gets a list of variable names corresponding to the binding IDs
     * in the pattern.
     * @return The list
     */
    public List<Token> getBindings() {
        return bindings;
    }
}
