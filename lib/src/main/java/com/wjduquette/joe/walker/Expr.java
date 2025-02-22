package com.wjduquette.joe.walker;

import java.util.List;

/**
 * The various kinds of expression that can appear in Joe's AST.
 */
sealed interface Expr
    permits Expr.Assign, Expr.Binary, Expr.Call,
            Expr.Get, Expr.Grouping, Expr.IndexGet, Expr.IndexSet,
            Expr.Lambda, Expr.ListLiteral, Expr.Literal, Expr.Logical,
            Expr.MapLiteral,
            Expr.PrePostAssign, Expr.PrePostIndex, Expr.PrePostSet,
            Expr.Set, Expr.Super, Expr.This, Expr.Ternary,
            Expr.Unary, Expr.Variable
{
    /**
     * An assignment to an existing variable.
     * See Stmt.Var for variable declaration, and Stmt.Set for assigning to
     * an object property.
     * @param name The variable's name token
     * @param op The assignment operator
     * @param value The expression to assign to it.
     */
    record Assign(Token name, Token op, Expr value) implements Expr {}

    /**
     * A binary expression, e.g., "+", "&lt;", etc.
     * See Expr.Logical for "&amp;&amp;" and "||" binaries.
     * @param left The left-hand expression
     * @param op The operator
     * @param right The right-hand expression
     */
    record Binary(Expr left, Token op, Expr right) implements Expr {}

    /**
     * A call to a function/method.  Note: arity checks are up to the
     * called function/method.
     * @param callee The expression that yields the function/method
     * @param paren A token, for line number info
     * @param arguments The argument expressions
     */
    record Call(Expr callee, Token paren, List<Expr> arguments) implements Expr {}

    /**
     * A get of an object property.
     * See Expr.Variable for a get of a normal variable.
     * @param object The expression that yields the object
     * @param name The name of the property
     */
    record Get(Expr object, Token name) implements Expr {}

    /**
     * A parenthesized expression
     * @param expr The expression in parentheses.
     */
    record Grouping(Expr expr) implements Expr {}

    /**
     * An index into a collection value.
     * called function/method.
     * @param collection The expression that yields the collection
     * @param bracket A token, for line number info
     * @param index The index expression
     */
    record IndexGet(Expr collection, Token bracket, Expr index) implements Expr {}

    /**
     * An index into a collection value.
     * called function/method.
     * @param collection The expression that yields the collection
     * @param bracket A token, for line number info
     * @param index The index expression
     * @param op The operator
     * @param value The value to set
     */
    record IndexSet(
        Expr collection,
        Token bracket,
        Expr index,
        Token op,
        Expr value
    ) implements Expr {}

    /**
     * A lambda function.
     * @param declaration The function's declaration
     */
    record Lambda(
        Stmt.Function declaration
    ) implements Expr {}

    /**
     * A List literal: a list of expressions used to initialize a
     * ListValue.
     * @param bracket The opening left bracket
     * @param list The list of expressions.
     */
    record ListLiteral(Token bracket, List<Expr> list) implements Expr {}

    /**
     * A literal value.
     * @param value The value.
     */
    record Literal(Object value) implements Expr {}

    /**
     * A logical binary operation.
     * See Expr.Binary for normal binary operations, e.g., "+".
     * @param left The left-hand expression
     * @param op The operator, "&amp;&amp;" or "||"
     * @param right The right-hand expression
     */
    record Logical(Expr left, Token op, Expr right) implements Expr {}

    /**
     * A Map literal: a list of expression pairs used to initialize a
     * MapValue.
     * @param brace The opening left brace
     * @param entries The flat list of expression pairs.
     */
    record MapLiteral(Token brace, List<Expr> entries) implements Expr {}

    /**
     * A pre-or-post increment/decrement to an existing variable.
     * @param name The variable's name token
     * @param op The operator
     * @param isPre Whether this is a pre-increment/decrement or not.
     */
    record PrePostAssign(Token name, Token op, boolean isPre) implements Expr {}

    /**
     * A pre-or-post increment/decrement to an indexed collection.
     * @param collection The expression that yields the collection.
     * @param bracket The bracket
     * @param index The expression that yields the index.
     * @param op The operator
     * @param isPre Whether this is a pre-increment/decrement or not.
     */
    record PrePostIndex(
        Expr collection,
        Token bracket,
        Expr index,
        Token op, boolean isPre) implements Expr {}
    /**
     * A pre-or-post increment/decrement to an existing property.
     * @param object The expression that yields the object.
     * @param name The name of the property
     * @param op The operator
     * @param isPre Whether this is a pre-increment/decrement or not.
     */
    record PrePostSet(Expr object, Token name, Token op, boolean isPre) implements Expr {}

    /**
     * An assignment to an object property.
     * See Expr.Assign for assignments to normal variables.
     * @param object The expression that yields the object.
     * @param name The name of the property
     * @param op The assignment operator
     * @param value The expression that yields the value to assign.
     */
    record Set(Expr object, Token name, Token op, Expr value) implements Expr {}

    /**
     * In a class method, a reference to a superclass method
     * @param keyword The "super" keyword
     * @param method The method name
     */
    record Super(Token keyword, Token method) implements Expr {}

    /**
     * A ternary operation
     * @param condition The condition
     * @param op The question mark operator token
     * @param trueExpr The true expression
     * @param falseExpr The false expression
     */
    record Ternary(Expr condition, Token op, Expr trueExpr, Expr falseExpr) implements Expr {}

    /**
     * In a class method, the magic "this" variable.
     * @param keyword The "this" keyword
     */
    record This(Token keyword) implements Expr {}

    /**
     * A unary operation
     * @param op "-" or "!"
     * @param right The expression yielding the value to be operated upon.
     */
    record Unary(Token op, Expr right) implements Expr {}

    /**
     * A get of a variable's value.
     * See Expr.Get for the get of an object's property's value.
     * @param name The variable name
     */
    record Variable(Token name) implements Expr {}
}
