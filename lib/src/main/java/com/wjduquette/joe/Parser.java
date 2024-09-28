package com.wjduquette.joe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static com.wjduquette.joe.TokenType.*;

class Parser {
    public static final String ARGS = "args";
    private static final int MAX_CALL_ARGUMENTS = 255;

    //-------------------------------------------------------------------------
    // Instance Variables

    private final Consumer<SyntaxError.Detail> reporter;
    private final List<Token> tokens;
    private int current = 0;

    //-------------------------------------------------------------------------
    // Constructor

    Parser(List<Token> tokens, Consumer<SyntaxError.Detail> reporter) {
        this.reporter = reporter;
        this.tokens = tokens;
    }

    //-------------------------------------------------------------------------
    // Public API

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    //-------------------------------------------------------------------------
    // Statements

    private Stmt declaration() {
        try {
            if (match(CLASS)) return classDeclaration();
            if (match(FUNCTION)) return functionDeclaration("function");
            if (match(VAR)) return varDeclaration();

            return statement();
        } catch (ErrorSync error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        // Class Name
        Token name = consume(IDENTIFIER, "Expected class name.");

        // Superclass
        Expr.Variable superclass = null;

        if (match(EXTENDS)) {
            consume(IDENTIFIER, "Expected superclass name.");
            superclass = new Expr.Variable(previous());
        }

        // Class body
        consume(LEFT_BRACE, "Expected '{' before class body.");

        List<Stmt.Function> staticMethods = new ArrayList<>();
        List<Stmt.Function> methods = new ArrayList<>();
        List<Stmt> staticInitializer = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            if (match(METHOD)) {
                methods.add(functionDeclaration("method"));
            } else if (match(STATIC)) {
                if (match(METHOD)) {
                    staticMethods.add(functionDeclaration("method"));
                } else {
                    consume(LEFT_BRACE,
                        "Expected 'method' or '{' after 'static'.");
                    staticInitializer.addAll(block());
                }
            } else {
                throw errorSync(advance(),
                    "Expected method, static method, or static initializer.");
            }
        }

        consume(RIGHT_BRACE, "Expected '}' after class body.");

        return new Stmt.Class(name, superclass,
            staticMethods, methods, staticInitializer);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expected variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expected ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(ASSERT)) return assertStatement();
        if (match(BREAK)) return breakStatement();
        if (match(CONTINUE)) return continueStatement();
        if (match(FOR)) return forStatement();
        if (match(FOREACH)) return forEachStatement();
        if (match(IF)) return ifStatement();
        if (match(RETURN)) return returnStatement();
        if (match(THROW)) return throwStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt assertStatement() {
        Token keyword = previous();
        Expr condition = expression();
        Expr message = null;

        if (match(COMMA)) {
            message = expression();
        }

        consume(SEMICOLON, "Expected ';' after assertion.");

        return new Stmt.Assert(keyword, condition, message);
    }

    private Stmt breakStatement() {
        Token keyword = previous();
        consume(SEMICOLON, "Expected ';' after 'break' value.");
        return new Stmt.Break(keyword);
    }

    private Stmt continueStatement() {
        Token keyword = previous();
        consume(SEMICOLON, "Expected ';' after 'continue' value.");
        return new Stmt.Continue(keyword);
    }

    private Stmt expressionStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expected ';' after expression.");
        return new Stmt.Expression(value);
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'for'.");

        // Initializer
        Stmt init;
        if (match(SEMICOLON)) {
            init = null;
        } else if (match(VAR)) {
            init = varDeclaration();
        } else {
            init = expressionStatement();
        }

        // Condition
        Expr condition = null;
        if (!check(SEMICOLON)) {
            System.out.println("B1");
            condition = expression();
        }
        consume(SEMICOLON, "Expected ';' after loop condition.");

        // Increment
        Expr incr = null;
        if (!check(RIGHT_PAREN)) {
            incr = expression();
        }
        consume(RIGHT_PAREN, "Expected ')' after loop clauses.");

        // Body
        Stmt body = statement();

        return new Stmt.For(init, condition, incr, body);
    }

    private Stmt forEachStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'foreach'.");
        consume(VAR, "Expected 'var' before loop variable name.");
        Token varName = consume(IDENTIFIER, "Expected loop variable name.");
        consume(COLON, "Expected ':' after loop variable name.");

        // List Expression
        Expr listExpr = expression();
        consume(RIGHT_PAREN, "Expected ')' after loop expression.");

        // Body
        var body = statement();

        return new Stmt.Block(List.of(
            new Stmt.Var(varName, null),
            new Stmt.ForEach(varName, listExpr, body)
        ));
    }

    private Stmt.Function functionDeclaration(String kind) {
        Token name = consume(IDENTIFIER, "Expected " + kind + " name.");

        consume(LEFT_PAREN, "Expected '(' after " + kind + " name.");

        List<Token> parameters = parameters(RIGHT_PAREN);

        consume(LEFT_BRACE, "Expected '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(kind, name, parameters, body);
    }

    private List<Token> parameters(TokenType terminator) {
        List<Token> parameters = new ArrayList<>();

        var names = new HashMap<String,Token>();

        if (!check(terminator)) {
            do {
                if (parameters.size() >= MAX_CALL_ARGUMENTS) {
                    error(peek(), "Expected no more than " + MAX_CALL_ARGUMENTS +
                        " parameters.");
                }

                var token = consume(IDENTIFIER, "Expected parameter name.");
                if (names.containsKey(token.lexeme())) {
                    throw errorSync(token, "Duplicate parameter name.");
                }
                names.put(token.lexeme(), token);
                parameters.add(token);
            } while (match(COMMA));
        }

        if (names.containsKey(ARGS) &&
            !parameters.getLast().lexeme().equals(ARGS)
        ) {
            throw errorSync(names.get(ARGS),
                "'args' must be the final parameter when present.");
        }
        var terminatorString = terminator == RIGHT_PAREN
            ? ")" : "->";

        consume(terminator, "Expected '" +
            terminatorString + "' after parameter list.");

        return parameters;
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;

        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expected ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt throwStatement() {
        Token keyword = previous();
        Expr value = expression();

        consume(SEMICOLON, "Expected ';' after thrown error.");
        return new Stmt.Throw(keyword, value);
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after condition.");
        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expected '}' after block.");
        return statements;
    }

    //-------------------------------------------------------------------------
    // Expressions

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = ternary();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name();
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get get) {
                return new Expr.Set(get.object(), get.name(), value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr ternary() {
        var expr = or();

        if (match(QUESTION)) {
            var token = previous();
            var trueExpr = or();
            consume(COLON, "Expected ':' after expression.");
            var falseExpr = or();
            return new Expr.Ternary(expr, token, trueExpr, falseExpr);
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, IN, LESS, LESS_EQUAL, NI)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expected property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= MAX_CALL_ARGUMENTS) {
                    error(peek(), "Call has more than " +
                        MAX_CALL_ARGUMENTS + " arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN,
            "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NULL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING, KEYWORD)) {
            return new Expr.Literal(previous().literal());
        }

        if (match(SUPER)) {
            Token keyword = previous();
            consume(DOT, "Expected '.' after 'super'.");
            Token method = consume(IDENTIFIER,
                "Expected superclass method name.");
            return new Expr.Super(keyword, method);
        }

        if (match(THIS)) return new Expr.This(previous());

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(BACK_SLASH)) {
            var token = previous();
            var parameters = parameters(MINUS_GREATER);

            List<Stmt> body;
            if (match(LEFT_BRACE)) {
                body = block();
            } else {
                var expr = expression();
                body = List.of(new Stmt.Return(token, expr));
            }
            var decl = new Stmt.Function("lambda", token, parameters, body);
            return new Expr.Lambda(decl);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw errorSync(peek(), "Expected expression.");
    }

    //-------------------------------------------------------------------------
    // Primitives

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw errorSync(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    // Saves the error detail, with no synchronization.
    void error(Token token, String message) {
        var line = token.line();
        var msg = token.type() == TokenType.EOF
            ? "Error at end: " + message
            : "Error at '" + token.lexeme() + "': " + message;
        reporter.accept(new SyntaxError.Detail(line, msg));
    }

    // Saves the error detail, with synchronization.
    private ErrorSync errorSync(Token token, String message) {
        error(token, message);
        return new ErrorSync(message);
    }

    // Discard tokens until we are at the beginning of the next statement.
    private void synchronize() {
        // Discard this token
        advance();

        while (!isAtEnd()) {
            // If we see we just completed a statement, return.
            if (previous().type() == SEMICOLON) return;

            // If we see a keyword indicating the beginning of a new
            // statement, return.
            switch (peek().type()) {
                case ASSERT:
                case CLASS:
                case FOR:
                case FUNCTION:
                case IF:
                case METHOD:
                case RETURN:
                case VAR:
                case WHILE:
                    return;
            }

            // Discard this token.
            advance();
        }
    }

    /**
     * An exception used to synchronize errors.
     */
    private static class ErrorSync extends RuntimeException {
        ErrorSync(String message) {
            super(message);
        }
    }
}
