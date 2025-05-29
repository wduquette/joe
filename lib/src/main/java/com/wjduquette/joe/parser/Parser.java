package com.wjduquette.joe.parser;
import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.scanner.Scanner;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.scanner.Token;
import com.wjduquette.joe.scanner.TokenType;
import com.wjduquette.joe.Trace;
import com.wjduquette.joe.patterns.Pattern;

import java.util.*;

import static com.wjduquette.joe.scanner.TokenType.*;

/**
 * The Joe parser.  Returns the parsed script as an AST consisting of a
 * list of {@link Stmt} records.
 */
public class Parser {
    public static final String ARGS = "args";
    private static final int MAX_CALL_ARGUMENTS = 255;

    //-------------------------------------------------------------------------
    // Static Methods

    // Used by Parser.isComplete()
    private static boolean isComplete;

    /**
     * Given the source, produces a dump of the Abstract Syntax Tree.
     * @param source The source
     * @return The dump
     * @throws SyntaxError on syntax errors
     */
    public static String dumpAST(String source) throws SyntaxError {
        // FIRST, parse the source.
        var buff = new SourceBuffer("*dumpAST*", source);
        var traces = new ArrayList<Trace>();
        var parser = new Parser(buff, (t, flag) -> traces.add(t));

        var statements = parser.parseJoe();

        if (!traces.isEmpty()) {
            throw new SyntaxError("Syntax error in input, halting.",
                traces, true);
        }

        // NEXT, produce the dump
        var dumper = new Dumper();
        return dumper.dump(statements);
    }

    /**
     * Returns true if this a complete Joe script (though possibly containing
     * errors), and false if it clearly stops partway through a construct.
     * @param source The source
     * @return true or false
     */
    public static boolean isComplete(String source) {
        var buff = new SourceBuffer("*isComplete*", source);
        var parser = new Parser(buff, Parser::completionReporter);

        // If it parsed without error then it is complete.  It might
        // also have resolution errors, but that's irrelevant.
        isComplete = true;
        parser.parseJoe();
        return isComplete;
    }

    private static void completionReporter(Trace ignored, boolean incomplete) {
        if (incomplete) isComplete = false;
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    private final SourceBuffer source;
    private final ErrorReporter reporter;
    private Scanner scanner = null;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new parser.
     * @param source The source to parse
     * @param reporter The error reporter.
     */
    public Parser(SourceBuffer source, ErrorReporter reporter) {
        this.source = source;
        this.reporter = reporter;
    }

    //-------------------------------------------------------------------------
    // Public API

    /**
     * Parses the source as Joe source code, attempting to detect as many
     * meaningful errors as possible.  Errors are reported via the parser's
     * error reporter.  If errors are reported then the result of this method
     * should be ignored.
     * @return The list of parsed statements.
     */
    public List<Stmt> parseJoe() {
        this.scanner = new Scanner(source, this::errorInScanner);
        List<Stmt> statements = new ArrayList<>();

        // FIRST, prime the scanner.  Any error detected while priming the
        // scanner will be reported but will not result in an `ErrorSync`.
        scanner.prime();

        while (!scanner.isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    // Handles scan errors, triggering synchronization.
    private void errorInScanner(SourceBuffer.Span span, String message) {
        reporter.reportError(new Trace(span, message), span.isAtEnd());
        if (scanner.isPrimed()) throw new ErrorSync(message);
    }

    //-------------------------------------------------------------------------
    // Statements

    private Stmt declaration() {
        try {
            if (scanner.match(CLASS)) return classDeclaration();
            if (scanner.match(FUNCTION)) return functionDeclaration(
                FunctionType.FUNCTION, "function");
            if (scanner.match(RECORD)) return recordDeclaration();
            if (scanner.match(VAR)) return varDeclaration();

            return statement();
        } catch (ErrorSync error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        // Class Name
        int start = scanner.previous().span().start();
        scanner.consume(IDENTIFIER, "Expected class name.");
        var name = scanner.previous();

        // Superclass
        Expr.VarGet superclass = null;

        if (scanner.match(EXTENDS)) {
            scanner.consume(IDENTIFIER, "Expected superclass name.");
            superclass = new Expr.VarGet(scanner.previous());
        }

        // Class body
        scanner.consume(LEFT_BRACE, "Expected '{' before class body.");

        List<Stmt.Function> staticMethods = new ArrayList<>();
        List<Stmt.Function> methods = new ArrayList<>();
        List<Stmt> staticInitializer = new ArrayList<>();

        while (!scanner.check(RIGHT_BRACE) && !scanner.isAtEnd()) {
            if (scanner.match(METHOD)) {
                methods.add(functionDeclaration(FunctionType.METHOD, "method"));
            } else if (scanner.match(STATIC)) {
                if (scanner.match(METHOD)) {
                    staticMethods.add(functionDeclaration(
                        FunctionType.STATIC_METHOD, "static method"));
                } else {
                    scanner.consume(LEFT_BRACE,
                        "Expected 'method' or '{' after 'static'.");
                    staticInitializer.addAll(block());
                }
            } else {
                scanner.advance();
                throw errorSync(scanner.previous(),
                    "Expected method, static method, or static initializer.");
            }
        }

        scanner.consume(RIGHT_BRACE, "Expected '}' after class body.");
        int end = scanner.previous().span().end();
        var classSpan = source.span(start, end);

        return new Stmt.Class(name, classSpan, superclass,
            staticMethods, methods, staticInitializer);
    }

    private Stmt.Function functionDeclaration(FunctionType type, String kind) {
        var start = scanner.previous().span().start();
        scanner.consume(IDENTIFIER, "Expected " + kind + " name.");
        var name = scanner.previous();
        if (type == FunctionType.METHOD && name.lexeme().equals("init")) {
            type = FunctionType.INITIALIZER;
        }

        scanner.consume(LEFT_PAREN, "Expected '(' after " + kind + " name.");

        List<Token> parameters = parameters(RIGHT_PAREN, false);

        scanner.consume(LEFT_BRACE, "Expected '{' before " + kind + " body.");
        List<Stmt> body = block();
        var end = scanner.previous().span().end();
        var span = source.span(start, end);
        return new Stmt.Function(type, name, parameters, body, span);
    }

    private List<Token> parameters(
        TokenType terminator,
        boolean isRecordParameters
    ) {
        List<Token> parameters = new ArrayList<>();

        var names = new HashMap<String,Token>();

        if (!scanner.check(terminator)) {
            do {
                if (parameters.size() >= MAX_CALL_ARGUMENTS) {
                    error(scanner.peek(), "Expected no more than " + MAX_CALL_ARGUMENTS +
                        " parameters.");
                }

                scanner.consume(IDENTIFIER, "Expected parameter name.");
                var token = scanner.previous();
                if (names.containsKey(token.lexeme())) {
                    throw errorSync(token, "Duplicate parameter name.");
                }
                names.put(token.lexeme(), token);
                parameters.add(token);
            } while (scanner.match(COMMA));
        }

        if (names.containsKey(ARGS)) {
            if (isRecordParameters) {
                throw errorSync(names.get(ARGS),
                    "A record type cannot have a variable argument list.");
            } else {
                if (!parameters.getLast().lexeme().equals(ARGS)) {
                    throw errorSync(names.get(ARGS),
                        "'args' must be the final parameter when present.");
                }
            }
        }

        var terminatorString = terminator == RIGHT_PAREN
            ? ")" : "->";

        scanner.consume(terminator, "Expected '" +
            terminatorString + "' after parameter list.");

        return parameters;
    }

    private Stmt recordDeclaration() {
        // Type Name
        int start = scanner.previous().span().start();
        scanner.consume(IDENTIFIER, "Expected record type name.");
        var name = scanner.previous();

        // Record Fields
        scanner.consume(LEFT_PAREN, "Expected '(' after record type name.");
        var recordFields = parameters(RIGHT_PAREN, true).stream()
            .map(Token::lexeme)
            .toList();

        if (recordFields.isEmpty()) {
            error(scanner.previous(), "Expected at least one record parameter.");
        }

        // Type body
        scanner.consume(LEFT_BRACE, "Expected '{' before type body.");

        List<Stmt.Function> staticMethods = new ArrayList<>();
        List<Stmt.Function> methods = new ArrayList<>();
        List<Stmt> staticInitializer = new ArrayList<>();

        while (!scanner.check(RIGHT_BRACE) && !scanner.isAtEnd()) {
            if (scanner.match(METHOD)) {
                methods.add(functionDeclaration(FunctionType.METHOD, "method"));
            } else if (scanner.match(STATIC)) {
                if (scanner.match(METHOD)) {
                    staticMethods.add(functionDeclaration(
                        FunctionType.STATIC_METHOD, "static method"));
                } else {
                    scanner.consume(LEFT_BRACE,
                        "Expected 'method' or '{' after 'static'.");
                    staticInitializer.addAll(block());
                }
            } else {
                scanner.advance();
                throw errorSync(scanner.previous(),
                    "Expected method, static method, or static initializer.");
            }
        }

        scanner.consume(RIGHT_BRACE, "Expected '}' after type body.");
        int end = scanner.previous().span().end();
        var span = source.span(start, end);

        return new Stmt.Record(name, span, recordFields,
            staticMethods, methods, staticInitializer);
    }

    private Stmt varDeclaration() {
        Token keyword = scanner.previous();
        ASTPattern pattern = pattern();

        return switch (pattern.getPattern()) {
            case Pattern.ValueBinding ignored -> {
                var name = scanner.previous();

                var initializer = scanner.match(EQUAL)
                    ? expression()
                    : new Expr.Null();

                scanner.consume(SEMICOLON, "Expected ';' after variable declaration.");
                yield new Stmt.Var(name, initializer);
            }
            case Pattern.Constant ignored ->
                throw errorSync(scanner.previous(), "Expected variable name.");
            case Pattern.Wildcard ignored ->
                throw errorSync(scanner.previous(), "Expected variable name.");
            default -> {
                if (pattern.getBindings().isEmpty()) {
                    error(scanner.previous(),
                        "'var' pattern must declare at least one variable.");
                }

                scanner.consume(EQUAL, "Expected '=' after pattern.");
                var target = expression();
                scanner.consume(SEMICOLON, "Expected ';' after target expression.");

                yield new Stmt.VarPattern(keyword, pattern, target);
            }
        };
    }

    private Stmt statement() {
        if (scanner.match(ASSERT)) return assertStatement();
        if (scanner.match(BREAK)) return breakStatement();
        if (scanner.match(CONTINUE)) return continueStatement();
        if (scanner.match(FOR)) return forStatement();
        if (scanner.match(FOREACH)) return forEachStatement();
        if (scanner.match(IF)) return scanner.match(LET) ? ifLetStatement() : ifStatement();
        if (scanner.match(MATCH)) return matchStatement();
        if (scanner.match(RETURN)) return returnStatement();
        if (scanner.match(SWITCH)) return switchStatement();
        if (scanner.match(THROW)) return throwStatement();
        if (scanner.match(WHILE)) return whileStatement();
        if (scanner.match(LEFT_BRACE)) return blockStatement();

        return expressionStatement();
    }

    private Stmt assertStatement() {
        Token keyword = scanner.previous();
        var conditionStart = keyword.span().end();
        Expr condition = expression();
        var conditionEnd = scanner.previous().span().end();
        Expr message;

        if (scanner.match(COMMA)) {
            message = expression();
        } else {
            var conditionText =
                source.span(conditionStart, conditionEnd).text().strip();
            message = new Expr.Literal("Assertion unmet: " + conditionText + ".");
        }

        scanner.consume(SEMICOLON, "Expected ';' after assertion.");

        return new Stmt.Assert(keyword, condition, message);
    }

    private Stmt breakStatement() {
        Token keyword = scanner.previous();
        scanner.consume(SEMICOLON, "Expected ';' after 'break' value.");
        return new Stmt.Break(keyword);
    }

    private Stmt continueStatement() {
        Token keyword = scanner.previous();
        scanner.consume(SEMICOLON, "Expected ';' after 'continue' value.");
        return new Stmt.Continue(keyword);
    }

    private Stmt expressionStatement() {
        Expr value = expression();
        scanner.consume(SEMICOLON, "Expected ';' after expression.");
        return new Stmt.Expression(value);
    }

    private Stmt forStatement() {
        var keyword = scanner.previous();
        var start = keyword.span().start();
        scanner.consume(LEFT_PAREN, "Expected '(' after 'for'.");

        // Initializer
        Stmt init;
        if (scanner.match(SEMICOLON)) {
            init = null;
        } else if (scanner.match(VAR)) {
            init = varDeclaration();
        } else {
            init = expressionStatement();
        }

        // Condition
        Expr condition = null;
        if (!scanner.check(SEMICOLON)) {
            condition = expression();
        }
        scanner.consume(SEMICOLON, "Expected ';' after loop condition.");

        // Increment
        Expr incr = null;
        if (!scanner.check(RIGHT_PAREN)) {
            incr = expression();
        }
        scanner.consume(RIGHT_PAREN, "Expected ')' after loop clauses.");

        // Body
        Stmt body = statement();

        // Wrap in a block so that the loop clauses have their local
        // scope.
        var end = scanner.previous().span().end();
        return new Stmt.Block(source.span(start, end),
            List.of(new Stmt.For(keyword, init, condition, incr, body)
        ));
    }

    private Stmt forEachStatement() {
        var keyword = scanner.previous();
        var start = keyword.span().start();
        scanner.consume(LEFT_PAREN, "Expected '(' after 'foreach'.");
        var pattern = pattern();

        switch (pattern.getPattern()) {
            case Pattern.Constant ignored ->
                throw errorSync(scanner.previous(), "Expected loop variable name.");
            case Pattern.Wildcard ignored ->
                throw errorSync(scanner.previous(), "Expected loop variable name.");
            case Pattern.ValueBinding ignored -> {
                var varName = scanner.previous();
                scanner.consume(COLON, "Expected ':' after loop variable name.");

                // List Expression
                Expr listExpr = expression();
                scanner.consume(RIGHT_PAREN, "Expected ')' after loop expression.");

                // Body
                var body = statement();
                var end = scanner.previous().span().end();

                return new Stmt.Block(
                    source.span(start, end),
                    List.of(
                        new Stmt.Var(varName, new Expr.Null()),
                        new Stmt.ForEach(keyword, varName, listExpr, body)
                    ));
            }
            default -> {
                scanner.consume(COLON, "Expected ':' after loop pattern.");

                // List Expression
                Expr listExpr = expression();
                scanner.consume(RIGHT_PAREN, "Expected ')' after loop expression.");

                // Body
                var body = statement();
                var end = scanner.previous().span().end();

                return new Stmt.Block(
                    source.span(start, end),
                    List.of(
                        new Stmt.ForEachBind(keyword, pattern, listExpr, body)
                    ));
            }
        }
    }


    private Stmt ifStatement() {
        var keyword = scanner.previous();
        scanner.consume(LEFT_PAREN, "Expected '(' after 'if'.");
        Expr condition = expression();
        scanner.consume(RIGHT_PAREN, "Expected ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (scanner.match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(keyword, condition, thenBranch, elseBranch);
    }

    private Stmt ifLetStatement() {
        scanner.consume(LEFT_PAREN, "Expected '(' after 'if let'.");
        Token keyword = scanner.previous();
        ASTPattern pattern = pattern();

        scanner.consume(EQUAL, "Expected '=' after pattern.");
        var target = expression();
        scanner.consume(RIGHT_PAREN, "Expected ')' after target expression.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (scanner.match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.IfLet(keyword, pattern, target, thenBranch, elseBranch);
    }

    private Stmt matchStatement() {
        Token keyword = scanner.previous();
        var cases = new ArrayList<Stmt.MatchCase>();

        // FIRST, parse the head of the statement
        scanner.consume(LEFT_PAREN, "Expected '(' after 'match'.");
        Expr target = expression();
        scanner.consume(RIGHT_PAREN, "Expected ')' after 'match' expression.");
        scanner.consume(LEFT_BRACE, "Expected '{' before 'match' body.");

        // NEXT, parse the cases.
        while (scanner.match(CASE)) {
            var caseKeyword = scanner.previous();
            var pattern = pattern();
            var guard = scanner.match(IF) ? expression() : null;
            scanner.consume(MINUS_GREATER, "Expected '->' after pattern.");
            var stmt = statement();
            cases.add(new Stmt.MatchCase(caseKeyword, pattern, guard, stmt));
        }

        if (cases.isEmpty()) {
            error(scanner.previous(), "Expected at least one 'case' in `match`.");
        }

        // NEXT, parse the default case, if it exists
        Stmt.MatchCase defCase = null;
        if (scanner.match(DEFAULT)) {
            var caseKeyword = scanner.previous();
            scanner.consume(MINUS_GREATER, "Expected '->' after 'default'.");
            var stmt = statement();
            defCase = new Stmt.MatchCase(caseKeyword, null, null, stmt);
        }

        // NEXT, complete the statement
        scanner.consume(RIGHT_BRACE, "Expected '}' after 'match' body.");

        return new Stmt.Match(keyword, target, cases, defCase);
    }

    private Stmt returnStatement() {
        Token keyword = scanner.previous();
        Expr value = null;

        if (!scanner.check(SEMICOLON)) {
            value = expression();
        }

        scanner.consume(SEMICOLON, "Expected ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt switchStatement() {
        Token keyword = scanner.previous();
        var cases = new ArrayList<Stmt.SwitchCase>();

        // FIRST, parse the head of the statement
        scanner.consume(LEFT_PAREN, "Expected '(' after 'switch'.");
        Expr switchExpr = expression();
        scanner.consume(RIGHT_PAREN, "Expected ')' after switch expression.");
        scanner.consume(LEFT_BRACE, "Expected '{' before switch body.");

        // NEXT, parse the cases.
        while (scanner.match(CASE)) {
            var caseKeyword = scanner.previous();
            var values = new ArrayList<Expr>();

            // FIRST, parse the values
            values.add(expression());
            while (scanner.match(COMMA)) {
                values.add(expression());
            }
            scanner.consume(MINUS_GREATER, "Expected '->' after case values.");
            var stmt = statement();
            cases.add(new Stmt.SwitchCase(caseKeyword, values, stmt));
        }

        if (cases.isEmpty()) {
            error(scanner.previous(), "Expected at least one 'case' in switch.");
        }

        // NEXT, parse the default case, if it exists
        Stmt.SwitchDefault switchDefault = null;
        if (scanner.match(DEFAULT)) {
            var defaultKeyword = scanner.previous();
            scanner.consume(MINUS_GREATER, "Expected '->' after 'default'.");
            var stmt = statement();
            switchDefault = new Stmt.SwitchDefault(defaultKeyword, stmt);
        }

        // NEXT, complete the statement
        scanner.consume(RIGHT_BRACE, "Expected '}' after switch body.");

        return new Stmt.Switch(keyword, switchExpr, cases, switchDefault);
    }

    private Stmt throwStatement() {
        Token keyword = scanner.previous();
        Expr value = expression();

        scanner.consume(SEMICOLON, "Expected ';' after thrown error.");
        return new Stmt.Throw(keyword, value);
    }

    private Stmt whileStatement() {
        var keyword = scanner.previous();
        scanner.consume(LEFT_PAREN, "Expected '(' after 'while'.");
        Expr condition = expression();
        scanner.consume(RIGHT_PAREN, "Expected ')' after condition.");
        Stmt body = statement();

        return new Stmt.While(keyword, condition, body);
    }

    private Stmt.Block blockStatement() {
        var start = scanner.previous().span().end();
        List<Stmt> statements = block();
        var end = scanner.previous().span().start();
        return new Stmt.Block(source.span(start, end), statements);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!scanner.check(RIGHT_BRACE) && !scanner.isAtEnd()) {
            statements.add(declaration());
        }

        scanner.consume(RIGHT_BRACE, "Expected '}' after block.");
        return statements;
    }

    //-------------------------------------------------------------------------
    // Expressions

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr target = ternary();

        if (scanner.match(EQUAL, PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, SLASH_EQUAL)) {
            Token op = scanner.previous();
            Expr value = assignment();

            if (target instanceof Expr.VarGet) {
                Token name = ((Expr.VarGet) target).name();
                return new Expr.VarSet(name, op, value);
            } else if (target instanceof Expr.PropGet get) {
                return new Expr.PropSet(get.object(), get.name(), op, value);
            } else if (target instanceof Expr.IndexGet get) {
                return new Expr.IndexSet(
                    get.collection(), get.bracket(), get.index(), op, value);
            }

            error(op, "Invalid assignment target.");
        }

        return target;
    }

    private Expr ternary() {
        var expr = or();

        if (scanner.match(QUESTION)) {
            var token = scanner.previous();
            var trueExpr = or();
            scanner.consume(COLON, "Expected ':' after expression.");
            var falseExpr = or();
            return new Expr.Ternary(expr, token, trueExpr, falseExpr);
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (scanner.match(OR)) {
            Token operator = scanner.previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (scanner.match(AND)) {
            Token operator = scanner.previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (scanner.match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = scanner.previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (scanner.match(GREATER, GREATER_EQUAL, IN, LESS, LESS_EQUAL, NI)) {
            Token operator = scanner.previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (scanner.match(MINUS, PLUS)) {
            Token operator = scanner.previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (scanner.match(SLASH, STAR)) {
            Token operator = scanner.previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (scanner.match(PLUS_PLUS, MINUS_MINUS)) {
            // Pre-increment/decrement
            Token op = scanner.previous();
            Expr target = unary();
            return prePost(op, target, true);
        } else if (scanner.match(BANG, MINUS)) {
            Token op = scanner.previous();
            Expr right = unary();

            return new Expr.Unary(op, right);
        }

        return postfix();
    }

    private Expr postfix() {
        Expr target = call();

        if (scanner.match(PLUS_PLUS, MINUS_MINUS)) {
            // Post-increment/decrement
            Token op = scanner.previous();
            return prePost(op, target, false);
        }

        return target;
    }

    private Expr prePost(Token op, Expr target, boolean isPre) {
        if (target instanceof Expr.VarGet) {
            Token name = ((Expr.VarGet) target).name();
            return new Expr.VarIncrDecr(name, op, isPre);
        } else if (target instanceof Expr.PropGet get) {
            return new Expr.PropIncrDecr(get.object(), get.name(), op, isPre);
        } else if (target instanceof Expr.IndexGet get) {
            return new Expr.IndexIncrDecr(
                get.collection(), get.bracket(), get.index(), op, isPre);
        }

        error(op, "Invalid '" + op.lexeme() + "' target.");
        return null;
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (scanner.match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (scanner.match(DOT)) {
                scanner.consume(IDENTIFIER, "Expected property name after '.'.");
                var name = scanner.previous();
                expr = new Expr.PropGet(expr, name);
            } else if (scanner.match(LEFT_BRACKET)) {
                var bracket = scanner.previous();
                var index = expression();
                scanner.consume(RIGHT_BRACKET, "Expected ']' after index.");
                expr = new Expr.IndexGet(expr, bracket, index);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!scanner.check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= MAX_CALL_ARGUMENTS) {
                    error(scanner.peek(), "Call has more than " +
                        MAX_CALL_ARGUMENTS + " arguments.");
                }
                arguments.add(expression());
            } while (scanner.match(COMMA));
        }

        scanner.consume(RIGHT_PAREN, "Expect ')' after arguments.");
        var paren = scanner.previous();

        return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {
        if (scanner.match(FALSE)) return new Expr.False();
        if (scanner.match(TRUE)) return new Expr.True();
        if (scanner.match(NULL)) return new Expr.Null();

        if (scanner.match(NUMBER, STRING, KEYWORD)) {
            return new Expr.Literal(scanner.previous().literal());
        }

        if (scanner.match(RULESET)) return ruleset();

        if (scanner.match(AT)) {
            Token keyword = scanner.previous();
            scanner.consume(IDENTIFIER, "Expected class property name.");
            var name = scanner.previous();
            var obj = new Expr.This(keyword);
            return new Expr.PropGet(obj, name);
        }


        if (scanner.match(SUPER)) {
            Token keyword = scanner.previous();
            scanner.consume(DOT, "Expected '.' after 'super'.");
            scanner.consume(IDENTIFIER,
                "Expected superclass method name.");
            var method = scanner.previous();
            return new Expr.Super(keyword, method);
        }

        if (scanner.match(THIS)) return new Expr.This(scanner.previous());

        if (scanner.match(IDENTIFIER)) {
            return new Expr.VarGet(scanner.previous());
        }

        if (scanner.match(BACK_SLASH)) {
            var token = scanner.previous();
            var parameters = parameters(MINUS_GREATER, false);

            List<Stmt> body;
            if (scanner.match(LEFT_BRACE)) {
                body = block();
            } else {
                var expr = expression();
                body = List.of(new Stmt.Return(token, expr));
            }
            var end = scanner.previous().span().end();
            var span = source.span(token.span().start(), end);
            var decl =
                new Stmt.Function(FunctionType.LAMBDA,
                    token, parameters, body, span);
            return new Expr.Lambda(decl);
        }

        if (scanner.match(LEFT_PAREN)) {
            Expr expr = expression();
            scanner.consume(RIGHT_PAREN, "Expected ')' after expression.");
            return new Expr.Grouping(expr);
        }

        // List literal
        if (scanner.match(LEFT_BRACKET)) {
            var bracket = scanner.previous();
            var list = new ArrayList<Expr>();

            if (!scanner.check(RIGHT_BRACKET)) {
                list.add(expression());

                while (scanner.match(COMMA)) {
                    // Allow trailing comma
                    if (scanner.check(RIGHT_BRACKET)) break;
                    list.add(expression());
                }
            }

            scanner.consume(RIGHT_BRACKET, "Expected ']' after list items.");

            return new Expr.ListLiteral(bracket, list);
        }

        // Map literal
        if (scanner.match(LEFT_BRACE)) {
            var brace = scanner.previous();
            var entries = new ArrayList<Expr>();

            if (!scanner.check(RIGHT_BRACE)) {
                entries.add(expression());
                scanner.consume(COLON, "Expected ':' after map key.");
                entries.add(expression());

                while (scanner.match(COMMA)) {
                    // Allow trailing comma
                    if (scanner.check(RIGHT_BRACE)) break;
                    entries.add(expression());
                    scanner.consume(COLON, "Expected ':' after map key.");
                    entries.add(expression());
                }
            }

            scanner.consume(RIGHT_BRACE, "Expected '}' after map entries.");

            return new Expr.MapLiteral(brace, entries);
        }

        throw errorSync(scanner.peek(), "Expected expression.");
    }

    //-------------------------------------------------------------------------
    // Patterns

    // Used to ensure that there are no duplicate binding variables in a
    // pattern.
    private transient Set<String> patternBindings;

    private ASTPattern pattern() {
        patternBindings = new HashSet<>();
        var walkerPattern = new ASTPattern();
        var pattern = parsePattern(walkerPattern, false); // Not a subpattern
        walkerPattern.setPattern(pattern);
        patternBindings = null;
        return walkerPattern;
    }

    private Pattern parsePattern(ASTPattern wp, boolean isSubpattern) {
        var constant = constantPattern(wp);

        if (constant != null) {
            return constant;
        }

        if (scanner.match(LEFT_BRACKET)) {
            return listPattern(wp);
        } else if (scanner.match(LEFT_BRACE)) {
            return mapPattern(wp);
        } else if (scanner.match(IDENTIFIER)) {
            var identifier = scanner.previous();

            if (identifier.lexeme().startsWith("_")) {
                return new Pattern.Wildcard(identifier.lexeme());
            } else if (scanner.match(LEFT_BRACE)) {
                return instancePattern(wp, identifier);
            } else if (scanner.match(LEFT_PAREN)) {
                return recordPattern(wp, identifier);
            }

            if (patternBindings.contains(identifier.lexeme())) {
                error(identifier, "Duplicate binding variable in pattern.");
            } else {
                patternBindings.add(identifier.lexeme());
            }

            wp.saveBinding(identifier);
            var name = identifier.lexeme();

            if (isSubpattern && scanner.match(EQUAL)) {
                var subpattern = parsePattern(wp, false);
                return new Pattern.PatternBinding(name, subpattern);
            } else {
                return new Pattern.ValueBinding(name);
            }
        } else {
            throw errorSync(scanner.peek(), "Expected pattern.");
        }
    }

    private Pattern.Constant constantPattern(ASTPattern wp) {
        if (scanner.match(TRUE)) {
            return wp.addLiteralConstant(true);
        } else if (scanner.match(FALSE)) {
            return wp.addLiteralConstant(false);
        } else if (scanner.match(NULL)) {
            return wp.addLiteralConstant(null);
        } else if (scanner.match(NUMBER) || scanner.match(STRING) || scanner.match(KEYWORD)) {
            return wp.addLiteralConstant(scanner.previous().literal());
        } else if (scanner.match(DOLLAR)) {
            if (scanner.match(IDENTIFIER)) {
                return wp.addVarConstant(scanner.previous());
            } else {
                scanner.consume(LEFT_PAREN, "Expected identifier or '(' after '$'.");
                var expr = expression();
                scanner.consume(RIGHT_PAREN,
                    "Expected ')' after interpolated expression.");
                return wp.addExprConstant(expr);
            }
        } else {
            return null;
        }
    }

    private Pattern listPattern(ASTPattern wp) {
        var list = new ArrayList<Pattern>();

        if (scanner.match(RIGHT_BRACKET)) {
            return new Pattern.ListPattern(list, null);
        }

        do {
            if (scanner.check(RIGHT_BRACKET) || scanner.check(COLON)) {
                break;
            }
            list.add(parsePattern(wp, true));
        } while (scanner.match(COMMA));

        String tailName = null;
        if (scanner.match(COLON)) {
            scanner.consume(IDENTIFIER, "Expected binding variable for list tail.");
            var tailVar = scanner.previous();
            wp.saveBinding(tailVar);
            tailName = tailVar.lexeme();
        }
        scanner.consume(RIGHT_BRACKET, "Expected ']' after list pattern.");

        return new Pattern.ListPattern(list, tailName);
    }

    private Pattern.MapPattern mapPattern(ASTPattern wp) {
        var map = new LinkedHashMap<Pattern.Constant,Pattern>();

        if (scanner.match(RIGHT_BRACE)) {
            return new Pattern.MapPattern(map);
        }

        do {
            if (scanner.check(RIGHT_BRACE)) {
                break;
            }
            var key = constantPattern(wp);
            scanner.consume(COLON, "Expected ':' after map key.");
            var value = parsePattern(wp, true);
            map.put(key, value);
        } while (scanner.match(COMMA));

        scanner.consume(RIGHT_BRACE, "Expected '}' after map pattern.");

        return new Pattern.MapPattern(map);
    }

    private Pattern instancePattern(ASTPattern wp, Token identifier) {
        var fieldMap = mapPattern(wp);
        return new Pattern.InstancePattern(identifier.lexeme(), fieldMap);
    }

    private Pattern recordPattern(ASTPattern wp, Token identifier) {
        var list = new ArrayList<Pattern>();

        if (scanner.match(RIGHT_PAREN)) {
            return new Pattern.RecordPattern(identifier.lexeme(), list);
        }

        do {
            if (scanner.check(RIGHT_PAREN)) {
                break;
            }
            list.add(parsePattern(wp, true));
        } while (scanner.match(COMMA));

        scanner.consume(RIGHT_PAREN, "Expected ')' after record pattern.");

        return new Pattern.RecordPattern(identifier.lexeme(), list);
    }

    //-------------------------------------------------------------------------
    // Nero Rule Sets

    /**
     * Parses the source as a standalone Nero program, attempting to detect
     * as many meaningful errors as possible.  Errors are reported via the
     * parser's error reporter.  If errors were reported, the result of
     * this method should be ignored.
     * @return The parsed rule set.
     */
    public Expr.RuleSet ruleset() {
        var keyword = scanner.previous();
        var facts = new ArrayList<ASTRuleSet.ASTIndexedAtom>();
        var rules = new ArrayList<ASTRuleSet.ASTRule>();

        scanner.consume(LEFT_BRACE, "expected '{' after 'ruleset'.");

        while (!scanner.match(RIGHT_BRACE)) {
            try {
                var head = head();

                if (scanner.match(SEMICOLON)) {
                    facts.add(fact(head));
                } else if (scanner.match(COLON_MINUS)) {
                    rules.add(rule(head));
                } else {
                    scanner.advance();
                    throw errorSync(scanner.previous(),
                        "expected fact or rule.");
                }
            } catch (ErrorSync error) {
                synchronize();
            }
        }

        return new Expr.RuleSet(keyword, new ASTRuleSet(facts, rules));
    }

    /**
     * Parses the source as a standalone Nero program, attempting to detect
     * as many meaningful errors as possible.  Errors are reported via the
     * parser's error reporter.  If errors were reported, the result of
     * this method should be ignored.
     * @return The parsed rule set.
     */
    public ASTRuleSet parseNero() {
        this.scanner = new Scanner(source, this::errorInScanner);
        scanner.prime();

        List<ASTRuleSet.ASTIndexedAtom> facts = new ArrayList<>();
        List<ASTRuleSet.ASTRule> rules = new ArrayList<>();

        while (!scanner.isAtEnd()) {
            try {
                var head = head();

                if (scanner.match(SEMICOLON)) {
                    facts.add(fact(head));
                } else if (scanner.match(COLON_MINUS)) {
                    rules.add(rule(head));
                } else {
                    scanner.advance();
                    throw errorSync(scanner.previous(),
                        "expected fact or rule.");
                }
            } catch (ErrorSync error) {
                synchronize();
            }
        }

        return new ASTRuleSet(facts, rules);
    }

    private ASTRuleSet.ASTIndexedAtom head() {
        // NEXT, parse the atom.
        scanner.consume(IDENTIFIER, "expected relation.");
        var relation = scanner.previous();
        scanner.consume(LEFT_PAREN, "expected '(' after relation.");
        return indexedAtom(relation);
    }

    private ASTRuleSet.ASTIndexedAtom fact(ASTRuleSet.ASTIndexedAtom head) {
        // Verify that there are no non-constant terms.
        for (var term : head.terms()) {
            if (!(term instanceof ASTRuleSet.ASTConstant)) {
                error(term.token(), "fact contains a non-constant term.");
            }
        }
        return head;
    }

    private ASTRuleSet.ASTRule rule(ASTRuleSet.ASTIndexedAtom head) {
        var body = new ArrayList<ASTRuleSet.ASTAtom>();
        var negations = new ArrayList<ASTRuleSet.ASTAtom>();
        var constraints = new ArrayList<ASTRuleSet.ASTConstraint>();

        var bodyVar = new HashSet<String>();
        do {
            var negated = scanner.match(NOT);

            var atom = bodyAtom();
            if (negated) {
                for (var name : atom.getVariableTokens()) {
                    if (!bodyVar.contains(name.lexeme())) {
                        error(name, "negated body atom contains unbound variable.");
                    }
                }
                negations.add(atom);
            } else {
                body.add(atom);
                bodyVar.addAll(atom.getVariableNames());
            }
        } while (scanner.match(COMMA));

        if (scanner.match(WHERE)) {
            constraints.add(constraint(bodyVar));
        } while (scanner.match(COMMA));

        scanner.consume(SEMICOLON, "expected ';' after rule body.");

        // Verify that the head contains only valid terms.
        for (var term : head.terms()) {
            switch (term) {
                case ASTRuleSet.ASTConstant ignored -> {}
                case ASTRuleSet.ASTVariable v -> {
                    if (!bodyVar.contains(v.token().lexeme())) {
                        error(v.token(),
                            "head atom contains unbound variable.");
                    }
                }
                case ASTRuleSet.ASTWildcard w -> error(w.token(),
                    "head atom contains wildcard.");
            }
        }

        return new ASTRuleSet.ASTRule(head, body, negations, constraints);
    }

    private ASTRuleSet.ASTConstraint constraint(Set<String> bodyVar) {
        var term = astTerm();
        Token op;
        ASTRuleSet.ASTVariable a = null;

        switch (term) {
            case ASTRuleSet.ASTConstant c ->
                error(c.token(), "expected bound variable.");
            case ASTRuleSet.ASTVariable v -> {
                a = v;
                if (!bodyVar.contains(v.token().lexeme())) {
                    error(v.token(), "expected bound variable.");
                }
            }
            case ASTRuleSet.ASTWildcard c ->
                error(c.token(), "expected bound variable.");
        }

        if (scanner.match(
            BANG_EQUAL, EQUAL_EQUAL,
            GREATER, GREATER_EQUAL,
            LESS, LESS_EQUAL)
        ) {
            op = scanner.previous();
        } else {
            scanner.advance();
            throw errorSync(scanner.previous(), "expected comparison operator.");
        }

        var b = astTerm();

        if (b instanceof ASTRuleSet.ASTVariable v) {
            if (!bodyVar.contains(v.token().lexeme())) {
                error(v.token(), "expected bound variable or constant.");
            }
        } else if (b instanceof ASTRuleSet.ASTWildcard w) {
            error(w.token(), "expected bound variable or constant.");
        }

        return new ASTRuleSet.ASTConstraint(a, op, b);
    }

    private ASTRuleSet.ASTAtom bodyAtom() {
        // NEXT, parse the atom.
        scanner.consume(IDENTIFIER, "expected relation.");
        var relation = scanner.previous();
        scanner.consume(LEFT_PAREN, "expected '(' after relation.");

        if (scanner.checkTwo(IDENTIFIER, COLON)) {
            return namedAtom(relation);
        } else {
            return indexedAtom(relation);
        }
    }

    private ASTRuleSet.ASTIndexedAtom indexedAtom(Token relation) {
        var terms = new ArrayList<ASTRuleSet.ASTTerm>();

        do {
            terms.add(astTerm());
        } while (scanner.match(COMMA));

        scanner.consume(RIGHT_PAREN, "expected ')' after terms.");

        return new ASTRuleSet.ASTIndexedAtom(relation, terms);
    }

    private ASTRuleSet.ASTNamedAtom namedAtom(Token relation) {
        var terms = new LinkedHashMap<Token,ASTRuleSet.ASTTerm>();

        do {
            scanner.consume(IDENTIFIER, "expected field name.");
            var name = scanner.previous();
            scanner.consume(COLON, "expected ':' after field name.");
            terms.put(name, astTerm());
        } while (scanner.match(COMMA));

        scanner.consume(RIGHT_PAREN, "expected ')' after terms.");

        return new ASTRuleSet.ASTNamedAtom(relation, terms);
    }

    private ASTRuleSet.ASTTerm astTerm() {
        if (scanner.match(IDENTIFIER)) {
            var name = scanner.previous();
            if (name.lexeme().startsWith("_")) {
                return new ASTRuleSet.ASTWildcard(name);
            } else {
                return new ASTRuleSet.ASTVariable(name);
            }
        } else if (scanner.match(KEYWORD, NUMBER, STRING)) {
            return new ASTRuleSet.ASTConstant(scanner.previous());
        } else {
            scanner.advance();
            throw errorSync(scanner.previous(), "expected term.");
        }
    }

    //-------------------------------------------------------------------------
    // Primitives

    // Saves the error detail, with no synchronization.
    void error(Token token, String message) {
        var msg = token.type() == TokenType.EOF
            ? "error at end, " + message
            : "error at '" + token.lexeme() + "', " + message;
        reporter.reportError(new Trace(token.span(), msg),
            token.type() == TokenType.EOF);
    }

    // Saves the error detail, with synchronization.
    private ErrorSync errorSync(Token token, String message) {
        error(token, message);
        return new ErrorSync(message);
    }

    // Discard tokens until we are at the beginning of the next statement.
    private void synchronize() {
        // Discard this token
        scanner.advance();

        while (!scanner.isAtEnd()) {
            // If we see we just completed a statement, return.
            if (scanner.previous().type() == SEMICOLON) return;

            // If we see a keyword indicating the beginning of a new
            // statement, return.
            switch (scanner.peek().type()) {
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
            scanner.advance();
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
