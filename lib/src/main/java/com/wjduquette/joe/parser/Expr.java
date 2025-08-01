package com.wjduquette.joe.parser;

import com.wjduquette.joe.SourceBuffer.Span;
import com.wjduquette.joe.nero.NeroRuleSet;
import com.wjduquette.joe.scanner.Token;
import java.util.List;

/**
 * The various kinds of expression that can appear in Joe's AST.
 */
public sealed interface Expr permits
    Expr.Binary,
    Expr.Call,
    Expr.False,
    Expr.Grouping,
    Expr.IndexGet, Expr.IndexIncrDecr, Expr.IndexSet,
    Expr.Lambda, Expr.ListLiteral, Expr.Literal, Expr.Logical,
    Expr.MapLiteral,
    Expr.Match,
    Expr.Null,
    Expr.PropGet, Expr.PropIncrDecr, Expr.PropSet,
    Expr.RuleSet,
    Expr.SetLiteral, Expr.Super,
    Expr.This, Expr.Ternary, Expr.True,
    Expr.Unary,
    Expr.VarGet, Expr.VarIncrDecr, Expr.VarSet
{
    /**
     * The location of the expression within the source.
     *
     * <p>Note: the span is not intended to capture the entire entity's
     * source text.  Rather, it is a strategically chosen to associate
     * byte-code generated for this entity with the relevant source
     * line.</p>
     * @return the location, or null.
     */
    default Span location() { return null; }

    /**
     * A binary expression, e.g., "+", "&lt;", etc.
     * See Expr.Logical for "&amp;&amp;" and "||" binaries.
     * @param left The left-hand expression
     * @param op The operator
     * @param right The right-hand expression
     */
    record Binary(Expr left, Token op, Expr right) implements Expr {
        public Span location() { return op.span(); }
    }

    /**
     * A call to a function/method.  Note: arity checks are up to the
     * called function/method.
     * @param callee The expression that yields the function/method
     * @param paren A token, for line number info
     * @param arguments The argument expressions
     */
    record Call(Expr callee, Token paren, List<Expr> arguments) implements Expr {
        public Span location() { return paren.span(); }
    }

    /**
     * A literal false
     */
    record False() implements Expr {}

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
    record IndexGet(Expr collection, Token bracket, Expr index) implements Expr {
        public Span location() { return bracket.span(); }
    }

    /**
     * A pre-or-post increment/decrement to an indexed collection.
     * @param collection The expression that yields the collection.
     * @param bracket The bracket
     * @param index The expression that yields the index.
     * @param op The operator
     * @param isPre Whether this is a pre-increment/decrement or not.
     */
    record IndexIncrDecr(
        Expr collection,
        Token bracket,
        Expr index,
        Token op, boolean isPre
    ) implements Expr {
        public Span location() { return op.span(); }
    }

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
    ) implements Expr {
        public Span location() { return op.span(); }
    }

    /**
     * A lambda function.
     * @param declaration The function's declaration
     */
    record Lambda(
        Stmt.Function declaration
    ) implements Expr {
        // TODO: Needs span
    }

    /**
     * A List literal: a list of expressions used to initialize a
     * ListValue.
     * @param bracket The opening left bracket
     * @param list The list of expressions.
     */
    record ListLiteral(Token bracket, List<Expr> list) implements Expr {
        public Span location() { return bracket.span(); }
    }

    /**
     * A literal value.
     * @param value The value.
     */
    record Literal(Object value) implements Expr {
        // TODO: Needs span?
    }

    /**
     * A logical binary operation.
     * See Expr.Binary for normal binary operations, e.g., "+".
     * @param left The left-hand expression
     * @param op The operator, "&amp;&amp;" or "||"
     * @param right The right-hand expression
     */
    record Logical(Expr left, Token op, Expr right) implements Expr {
        public Span location() { return op.span(); }
    }

    /**
     * A Map literal: a list of expression pairs used to initialize a
     * MapValue.
     * @param brace The opening left brace
     * @param entries The flat list of expression pairs.
     */
    record MapLiteral(Token brace, List<Expr> entries) implements Expr {
        public Span location() { return brace.span(); }
    }

    /**
     * A matching expression, `value ~ pattern`.
     * @param target The left-hand expression
     * @param op The operator
     * @param pattern The right-hand pattern
     */
    record Match(Expr target, Token op, ASTPattern pattern) implements Expr {
        public Span location() { return op.span(); }
    }

    /**
     * A literal null
     */
    record Null() implements Expr {}

    /**
     * A get of an object property.
     * See Expr.Variable for a get of a normal variable.
     * @param object The expression that yields the object
     * @param name The name of the property
     */
    record PropGet(Expr object, Token name) implements Expr {
        public Span location() { return name.span(); }
    }

    /**
     * A pre-or-post increment/decrement to an existing property.
     * @param object The expression that yields the object.
     * @param name The name of the property
     * @param op The operator
     * @param isPre Whether this is a pre-increment/decrement or not.
     */
    record PropIncrDecr(
        Expr object,
        Token name,
        Token op,
        boolean isPre
    ) implements Expr {
        public Span location() { return op.span(); }
    }

    /**
     * An assignment to an object property.
     * See Expr.Assign for assignments to normal variables.
     * @param object The expression that yields the object.
     * @param name The name of the property
     * @param op The assignment operator
     * @param value The expression that yields the value to assign.
     */
    record PropSet(Expr object, Token name, Token op, Expr value) implements Expr {
        public Span location() { return op.span(); }
    }

    /**
     * A `ruleset` expression, containing a Nero rule set.
     * @param keyword The `ruleset` keyword
     * @param ruleSet The rule set as parsed
     */
    record RuleSet(
        Token keyword,
        NeroRuleSet ruleSet
    ) implements Expr {
        public Span location() { return keyword.span(); }
    }

    /**
     * A Set literal: a list of expressions used to initialize a
     * SetValue.
     * @param bracket The opening left bracket
     * @param list The list of expressions.
     */
    record SetLiteral(Token bracket, List<Expr> list) implements Expr {
        public Span location() { return bracket.span(); }
    }

    /**
     * In a class method, a reference to a superclass method
     * @param keyword The "super" keyword
     * @param method The method name
     */
    record Super(Token keyword, Token method) implements Expr {
        public Span location() { return keyword.span(); }
    }

    /**
     * A ternary operation
     * @param condition The condition
     * @param op The question mark operator token
     * @param trueExpr The true expression
     * @param falseExpr The false expression
     */
    record Ternary(Expr condition, Token op, Expr trueExpr, Expr falseExpr) implements Expr {
        public Span location() { return op.span(); }
    }

    /**
     * In a class method, the magic "this" variable.
     * @param keyword The "this" keyword
     */
    record This(Token keyword) implements Expr {
        public Span location() { return keyword.span(); }
    }

    /**
     * A literal true
     */
    record True() implements Expr {}

    /**
     * A unary operation
     * @param op "-" or "!"
     * @param right The expression yielding the value to be operated upon.
     */
    record Unary(Token op, Expr right) implements Expr {
        public Span location() { return op.span(); }
    }

    /**
     * A get of a variable's value.
     * See Expr.Get for the get of an object's property's value.
     * @param name The variable name
     */
    record VarGet(Token name) implements Expr {
        public Span location() { return name.span(); }
    }

    /**
     * A pre-or-post increment/decrement to an existing variable.
     * @param name The variable's name token
     * @param op The operator
     * @param isPre Whether this is a pre-increment/decrement or not.
     */
    record VarIncrDecr(Token name, Token op, boolean isPre) implements Expr {
        public Span location() { return op.span(); }
    }

    /**
     * An assignment to an existing variable.
     * @param name The variable's name token
     * @param op The assignment operator, `=`, `+=`, etc.
     * @param value The expression to assign to it.
     */
    record VarSet(Token name, Token op, Expr value) implements Expr {
        public Span location() { return name.span(); }
    }
}
