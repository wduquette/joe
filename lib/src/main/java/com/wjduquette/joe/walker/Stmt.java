package com.wjduquette.joe.walker;

import java.util.List;
import com.wjduquette.joe.scanner.SourceBuffer.Span;

/**
 * The statements that can appear in a Joe AST.
 */
sealed interface Stmt
    permits Stmt.Assert, Stmt.Block, Stmt.Break, Stmt.Class,
            Stmt.Continue, Stmt.Expression, Stmt.For, Stmt.ForEach,
            Stmt.Function, Stmt.If, Stmt.IfLet, Stmt.Let, Stmt.Match,
            Stmt.Record, Stmt.Return, Stmt.Switch,
            Stmt.Throw, Stmt.Var, Stmt.While
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
     * A break statement in a loop.
     * @param token The token, for line number info.
     */
    record Break(Token token) implements Stmt {}

    /**
     * A class declaration
     * @param name The class name
     * @param classSpan The class's span in the source script.
     * @param superclass The superclass variable, or null for none
     * @param staticMethods The class object's static methods
     * @param methods The class's instance methods
     * @param staticInitializer The static initializer statements
     */
    record Class(
        Token name,
        Span classSpan,
        Expr.Variable superclass,
        List<Stmt.Function> staticMethods,
        List<Stmt.Function> methods,
        List<Stmt> staticInitializer
    ) implements Stmt {}

    /**
     * A continue statement in a loop.
     * @param token The token, for line number info.
     */
    record Continue(Token token) implements Stmt {}

    /**
     * An expression statement: an expression followed by a ";"
     * @param expr The expression
     */
    record Expression(Expr expr) implements Stmt {}

    /**
     * A "for" loop
     * @param init The initializer, e.g., "var i = 0"
     * @param condition The condition, e.g., "i &lt; 10"
     * @param incr The incrementer, e.g., "i = i + 1"
     * @param body The body of the loop, a statement or block.
     */
    record For(Stmt init, Expr condition, Expr incr, Stmt body) implements Stmt {}

    /**
     * A "foreach" loop
     * @param varName The loop variable
     * @param listExpr The list expression
     * @param body The body of the loop, a statement or block.
     */
    record ForEach(Token varName, Expr listExpr, Stmt body) implements Stmt {}

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

    /** An "if let" statement.
     * @param keyword The `let` keyword
     * @param pattern The pattern being matched
     * @param target The target expression
     * @param thenBranch Statement or block to execute if true
     * @param elseBranch Statement or block to execute if false, or null
     */
    record IfLet(
        Token keyword,
        WalkerPattern pattern,
        Expr target,
        Stmt thenBranch,
        Stmt elseBranch
    ) implements Stmt {}

    /**
     * A "let" statement
     * @param keyword The keyword, for error location
     * @param pattern The pattern to match
     * @param target The target expression
     */
    record Let(Token keyword, WalkerPattern pattern, Expr target)
        implements Stmt {}

    /**
     * A "match" statement.
     * @param keyword The "match" keyword
     * @param expr The match expression
     * @param cases A list of cases
     */
    record Match(
        Token keyword,
        Expr expr,
        List<MatchCase> cases
    ) implements Stmt {}

    /**
     * A case in a match statement.
     * @param keyword The "case" keyword
     * @param pattern The pattern to match
     * @param guard The guard condition, or null.
     * @param statement The statement to execute
     */
    record MatchCase(
        Token keyword,
        WalkerPattern pattern,
        Expr guard,
        Stmt statement
    ) {}

    /**
     * A record declaration
     * @param name The type's name
     * @param typeSpan The type's span in the source script.
     * @param recordFields The type's field names.
     * @param staticMethods The class object's static methods
     * @param methods The class's instance methods
     * @param staticInitializer The static initializer statements
     */
    record Record(
        Token name,
        Span typeSpan,
        List<String> recordFields,
        List<Stmt.Function> staticMethods,
        List<Stmt.Function> methods,
        List<Stmt> staticInitializer
    ) implements Stmt {}

    /**
     * A "return" statement
     * @param keyword The return keyword, for error location
     * @param value The value, or null
     */
    record Return(Token keyword, Expr value) implements Stmt {}

    /**
     * A "switch" statement.
     * @param keyword The "switch" keyword
     * @param expr The switch expression
     * @param cases A list of cases
     */
    record Switch(
        Token keyword,
        Expr expr,
        List<SwitchCase> cases
    ) implements Stmt {}

    /**
     * A case in a switch statement.
     * @param keyword The "case" keyword
     * @param values The value expressions for this case.
     * @param statement The statement to execute
     */
    record SwitchCase(
        Token keyword,
        List<Expr> values,
        Stmt statement
    ) {}

    /**
     * A "throw" statement
     * @param keyword The throw keyword, for error location
     * @param value The error value
     */
    record Throw(Token keyword, Expr value) implements Stmt {}

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
