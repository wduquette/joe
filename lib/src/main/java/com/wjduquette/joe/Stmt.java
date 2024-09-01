package com.wjduquette.joe;

import java.util.List;

public sealed interface Stmt
    permits Stmt.Assert, Stmt.Block, Stmt.Class, Stmt.Expression,
            Stmt.Function, Stmt.For, Stmt.If, Stmt.Print, Stmt.Return,
            Stmt.Var, Stmt.While
{
    /**
     * Asserts that the condition is truthy, throwing an AssertError
     * otherwise.  If the message is omitted, a generated message will
     * be used.
     * @param keyword The token, for line number info.
     * @param condition The condition to test
     * @param message The failure message, or null for the default
     */
    record Assert(Token keyword, Expr condition, Expr message) implements Stmt {}

    /**
     * A block of statements, surrounded by braces.
     * @param statements The statements
     */
    record Block(List<Stmt> statements) implements Stmt {}

    /**
     * A class declaration
     * @param name The class name
     * @param superclass The superclass variable, or null for none
     * @param methods The class's methods
     */
    record Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods) implements Stmt {}

    /**
     * An expression statement: an expression followed by a ";"
     * @param expr The expression
     */
    record Expression(Expr expr) implements Stmt {}

    /**
     * A "for" loop
     * @param init The initializer, e.g., "var i = 0"
     * @param condition The condition, e.g., "i < 10"
     * @param incr The incrementer, e.g., "i = i + 1"
     * @param body The body of the loop, a statement or block.
     */
    record For(Stmt init, Expr condition, Expr incr, Stmt body) implements Stmt {}

    /**
     * A function or method.
     * @param kind The kind, currently "function" or "method"
     * @param name The name
     * @param params The parameter names
     * @param body The body of the function
     */
    record Function(String kind, Token name, List<Token> params, List<Stmt> body) implements Stmt {}

    /** An "if" statement.
     * @param condition The condition being tested
     * @param thenBranch Statement or block to execute if true
     * @param elseBranch Statement or block to execute if false, or null
     */
    record If(Expr condition, Stmt thenBranch, Stmt elseBranch) implements Stmt {}

    /**
     * A "print" statement (to be removed eventually)
     * @param expr The expression to print.
     */
    record Print(Expr expr) implements Stmt {}

    /**
     * A "return" statement
     * @param keyword The return keyword, for error location
     * @param value The value, or null
     */
    record Return(Token keyword, Expr value) implements Stmt {}

    /**
     * A "var" variable declaration.
     * @param name The variable name
     * @param initializer The initializer, or null
     */
    record Var(Token name, Expr initializer) implements Stmt {}

    /**
     * A "while" loop
     * @param condition The loop condition
     * @param body The statement or block to execute.
     */
    record While(Expr condition, Stmt body) implements Stmt {}
}
