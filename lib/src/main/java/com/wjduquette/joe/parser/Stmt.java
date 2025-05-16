package com.wjduquette.joe.parser;

import com.wjduquette.joe.scanner.Token;
import com.wjduquette.joe.SourceBuffer.Span;

import java.util.List;

/**
 * The statements that can appear in a Joe AST.
 */
public sealed interface Stmt
    permits Stmt.Assert, Stmt.Block, Stmt.Break, Stmt.Class,
            Stmt.Continue, Stmt.Expression, Stmt.For, Stmt.ForEach,
            Stmt.Function, Stmt.If, Stmt.IfLet, Stmt.Let, Stmt.Match,
            Stmt.Record, Stmt.Return, Stmt.Switch,
            Stmt.Throw, Stmt.Var, Stmt.While
{
    /**
     * The location of the statement within the source.
     *
     * <p>Note: the span is not intended to capture the entire entity's
     * source text.  Rather, it is a strategically chosen to associate
     * byte-code generated for this entity with the relevant source
     * line.</p>
     * @return the location, or null.
     */
    default Span location() { return null; }

    /**
     * Asserts that the condition is truthy, throwing an AssertError
     * otherwise.  If the message is omitted, a generated message will
     * be used.
     * @param keyword The token, for line number info.
     * @param condition The condition to test
     * @param message The failure message, or null for the default
     */
    record Assert(Token keyword, Expr condition, Expr message) implements Stmt {
        public Span location() { return keyword.span(); }
    }

    /**
     * A block of statements, surrounded by braces.
     * @param span The block's complete span
     * @param statements The statements
     */
    record Block(Span span, List<Stmt> statements) implements Stmt {
        public Span location() { return span; }
    }

    /**
     * A break statement in a loop.
     * @param keyword The token, for line number info.
     */
    record Break(Token keyword) implements Stmt {
        public Span location() { return keyword.span(); }
    }

    /**
     * A class declaration
     * @param name The class name
     * @param classSpan The class's span in the source script.
     * @param superclass The superclass variable, or null for none
     * @param staticMethods The class object's static methods
     * @param methods The class's instance methods
     * @param staticInit The static initializer statements
     */
    record Class(
        Token name,
        Span classSpan,
        Expr.VarGet superclass,
        List<Stmt.Function> staticMethods,
        List<Stmt.Function> methods,
        List<Stmt> staticInit
    ) implements Stmt {
        public Span location() { return classSpan; }
    }

    /**
     * A continue statement in a loop.
     * @param keyword The token, for line number info.
     */
    record Continue(Token keyword) implements Stmt {
        public Span location() { return keyword.span(); }
    }

    /**
     * An expression statement: an expression followed by a ";"
     * @param expr The expression
     */
    record Expression(Expr expr) implements Stmt {
        // TODO need span
    }

    /**
     * A "for" loop
     * @param keyword The "for" keyword
     * @param init The initializer, e.g., "var i = 0"
     * @param condition The condition, e.g., "i &lt; 10"
     * @param updater The incrementer, e.g., "i = i + 1"
     * @param body The body of the loop, a statement or block.
     */
    record For(
        Token keyword,
        Stmt init,
        Expr condition,
        Expr updater,
        Stmt body
    ) implements Stmt {
        public Span location() { return keyword.span(); }
    }

    /**
     * A "foreach" loop
     * @param keyword The "foreach" keyword
     * @param name The loop variable
     * @param items The collection expression
     * @param body The body of the loop, a statement or block.
     */
    record ForEach(
        Token keyword,
        Token name,
        Expr items,
        Stmt body
    ) implements Stmt {
        public Span location() { return keyword.span(); }
    }

    /**
     * A function, method, etc.
     * @param type The function type
     * @param name The name
     * @param params The parameter names
     * @param body The body of the function
     * @param span The function's full span.
     */
    record Function(
        FunctionType type,
        Token name,
        List<Token> params,
        List<Stmt> body,
        Span span
    ) implements Stmt {
        public Span location() { return span; }
    }

    /** An "if" statement.
     * @param keyword The "if" keyword
     * @param condition The condition being tested
     * @param thenBranch Statement or block to execute if true
     * @param elseBranch Statement or block to execute if false, or null
     */
    record If(
        Token keyword,
        Expr condition,
        Stmt thenBranch,
        Stmt elseBranch
    ) implements Stmt {
        public Span location() { return keyword.span(); }
    }

    /** An "if let" statement.
     * @param keyword The `let` keyword
     * @param pattern The pattern being matched
     * @param target The target expression
     * @param thenBranch Statement or block to execute if true
     * @param elseBranch Statement or block to execute if false, or null
     */
    record IfLet(
        Token keyword,
        ASTPattern pattern,
        Expr target,
        Stmt thenBranch,
        Stmt elseBranch
    ) implements Stmt {
        public Span location() { return keyword.span(); }
    }

    /**
     * A "let" statement
     * @param keyword The keyword, for error location
     * @param pattern The pattern to match
     * @param target The target expression
     */
    record Let(Token keyword, ASTPattern pattern, Expr target) implements Stmt {
        public Span location() { return keyword.span(); }
    }

    /**
     * A "match" statement.
     * @param keyword The "match" keyword
     * @param expr The match expression
     * @param cases A list of cases
     * @param matchDefault The default case, or null
     */
    record Match(
        Token keyword,
        Expr expr,
        List<MatchCase> cases,
        MatchCase matchDefault
    ) implements Stmt {
        public Span location() { return keyword.span(); }
    }

    /**
     * A case in a match statement.
     * @param keyword The "case" keyword
     * @param pattern The pattern to match
     * @param guard The guard condition, or null.
     * @param statement The statement to execute
     */
    record MatchCase(
        Token keyword,
        ASTPattern pattern,
        Expr guard,
        Stmt statement
    ) {
        public Span location() { return keyword.span(); }
    }

    /**
     * A record declaration
     * @param name The type's name
     * @param typeSpan The type's span in the source script.
     * @param fields The type's field names.
     * @param staticMethods The class object's static methods
     * @param methods The class's instance methods
     * @param staticInit The static initializer statements
     */
    record Record(
        Token name,
        Span typeSpan,
        List<String> fields,
        List<Stmt.Function> staticMethods,
        List<Stmt.Function> methods,
        List<Stmt> staticInit
    ) implements Stmt {
        public Span location() { return name.span(); }
    }

    /**
     * A "return" statement
     * @param keyword The return keyword, for error location
     * @param value The value, or null
     */
    record Return(Token keyword, Expr value) implements Stmt {
        public Span location() { return keyword.span(); }
    }

    /**
     * A "switch" statement.
     * @param keyword The "switch" keyword
     * @param expr The switch expression
     * @param cases A list of cases
     * @param switchDefault The default case, or null
     */
    record Switch(
        Token keyword,
        Expr expr,
        List<SwitchCase> cases,
        SwitchDefault switchDefault
    ) implements Stmt {
        public Span location() { return keyword.span(); }
    }

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
    ) {
        public Span location() { return keyword.span(); }
    }

    /**
     * The default case in a switch statement.
     * @param keyword The "default" keyword
     * @param statement The statement to execute
     */
    record SwitchDefault(
        Token keyword,
        Stmt statement
    ) {
        public Span location() { return keyword.span(); }
    }

    /**
     * A "throw" statement
     * @param keyword The throw keyword, for error location
     * @param value The error value
     */
    record Throw(Token keyword, Expr value) implements Stmt {
        public Span location() { return keyword.span(); }
    }

    /**
     * A "var" variable declaration.  The initializer should always be
     * a valid Expr, possibly Expr.Literal(null).
     * @param name The variable name
     * @param value The initializer, NOT null
     */
    record Var(Token name, Expr value) implements Stmt {
        public Span location() { return name.span(); }
    }

    /**
     * A "while" loop
     * @param keyword The "while" keyword
     * @param condition The loop condition
     * @param body The statement or block to execute.
     */
    record While(Token keyword, Expr condition, Stmt body) implements Stmt {
        public Span location() { return keyword.span(); }
    }
}
