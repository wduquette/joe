package com.wjduquette.joe.parser;
import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.nero.NeroRuleSet;
import com.wjduquette.joe.scanner.Scanner;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.scanner.Token;
import com.wjduquette.joe.scanner.TokenType;
import com.wjduquette.joe.Trace;
import com.wjduquette.joe.patterns.Pattern;

import java.util.*;
import java.util.stream.Collectors;

import static com.wjduquette.joe.scanner.TokenType.*;

/**
 * The Joe parser.  Returns the parsed script as an AST consisting of a
 * list of {@link Stmt} records.
 */
public class Parser {
    /** The name of the "varargs" argument. */
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
        return ASTDumper.dump(statements);
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
    private boolean synchronizing = false;

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
        if (!synchronizing) {
            reporter.reportError(new Trace(span, message), span.isAtEnd());
            if (scanner.isPrimed()) throw new ErrorSync(message);
        }
    }

    //-------------------------------------------------------------------------
    // Statements

    private Stmt declaration() {
        try {
            var isExported = scanner.match(EXPORT);
            var exportToken = scanner.previous();

            if (scanner.match(CLASS)) return classDeclaration(isExported);
            if (scanner.match(FUNCTION)) return functionDeclaration(
                FunctionType.FUNCTION, "function", isExported);
            if (scanner.match(RECORD)) return recordDeclaration(isExported);

            if (isExported) {
                error(exportToken,
                    "expected 'export' to be followed by 'function', 'class', or 'record'.");
            }

            if (scanner.match(IMPORT)) return importDeclaration();
            if (scanner.match(VAR)) return varDeclaration();

            return statement();
        } catch (ErrorSync error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration(boolean isExported) {
        // Class Name
        int start = scanner.previous().span().start();
        scanner.consume(IDENTIFIER, "expected class name.");
        var name = scanner.previous();

        // Superclass
        Expr.VarGet superclass = null;

        if (scanner.match(EXTENDS)) {
            scanner.consume(IDENTIFIER, "expected superclass name.");
            superclass = new Expr.VarGet(scanner.previous());
        }

        // Class body
        scanner.consume(LEFT_BRACE, "expected '{' before class body.");

        List<Stmt.Function> staticMethods = new ArrayList<>();
        List<Stmt.Function> methods = new ArrayList<>();
        List<Stmt> staticInitializer = new ArrayList<>();

        while (!scanner.check(RIGHT_BRACE) && !scanner.isAtEnd()) {
            if (scanner.match(METHOD)) {
                methods.add(
                    functionDeclaration(FunctionType.METHOD, "method", false));
            } else if (scanner.match(STATIC)) {
                if (scanner.match(METHOD)) {
                    staticMethods.add(functionDeclaration(
                        FunctionType.STATIC_METHOD, "static method", false));
                } else {
                    scanner.consume(LEFT_BRACE,
                        "expected 'method' or '{' after 'static'.");
                    staticInitializer.addAll(block());
                }
            } else {
                scanner.advance();
                throw errorSync(scanner.previous(),
                    "expected method, static method, or static initializer.");
            }
        }

        scanner.consume(RIGHT_BRACE, "expected '}' after class body.");
        int end = scanner.previous().span().end();
        var classSpan = source.span(start, end);

        return new Stmt.Class(name, isExported, classSpan, superclass,
            staticMethods, methods, staticInitializer);
    }

    private Stmt.Function functionDeclaration(
        FunctionType type,
        String kind,
        boolean isExported
    ) {
        var start = scanner.previous().span().start();
        scanner.consume(IDENTIFIER, "expected " + kind + " name.");
        var name = scanner.previous();
        if (type == FunctionType.METHOD && name.lexeme().equals("init")) {
            type = FunctionType.INITIALIZER;
        }

        scanner.consume(LEFT_PAREN, "expected '(' after " + kind + " name.");

        List<Token> parameters = parameters(RIGHT_PAREN, false);

        scanner.consume(LEFT_BRACE, "expected '{' before " + kind + " body.");
        List<Stmt> body = block();
        var end = scanner.previous().span().end();
        var span = source.span(start, end);
        return new Stmt.Function(type, isExported, name, parameters, body, span);
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
                    error(scanner.peek(), "expected no more than " + MAX_CALL_ARGUMENTS +
                        " parameters.");
                }

                scanner.consume(IDENTIFIER, "expected parameter name.");
                var token = scanner.previous();
                if (names.containsKey(token.lexeme())) {
                    throw errorSync(token, "duplicate parameter name.");
                }
                names.put(token.lexeme(), token);
                parameters.add(token);
            } while (scanner.match(COMMA));
        }

        if (names.containsKey(ARGS)) {
            if (isRecordParameters) {
                throw errorSync(names.get(ARGS),
                    "record type cannot have a variable-length argument list.");
            } else {
                if (!parameters.getLast().lexeme().equals(ARGS)) {
                    throw errorSync(names.get(ARGS),
                        "'args' must be the final parameter when present.");
                }
            }
        }

        var terminatorString = terminator == RIGHT_PAREN
            ? ")" : "->";

        scanner.consume(terminator, "expected '" +
            terminatorString + "' after parameter list.");

        return parameters;
    }

    private Stmt importDeclaration() {
        var keyword = scanner.previous();
        var spec = new ArrayList<Token>();

        scanner.consume(IDENTIFIER, "expected package name component after 'import'.");
        spec.add(scanner.previous());
        scanner.consume(DOT, "expected '.' after package name component.");

        do {
            if (scanner.match(IDENTIFIER)) {
                spec.add(scanner.previous());
            } else if (scanner.match(STAR)) {
                spec.add(scanner.previous());
                break;
            }
        } while (scanner.match(DOT));

        if (spec.size() == 1) {
            error(scanner.peek(),
                "expected symbol name or '*' after '.'.");
        }

        scanner.consume(SEMICOLON, "expected ';' after import spec.");


        var symbol = spec.removeLast().lexeme();
        var pkgName = spec.stream()
            .map(Token::lexeme)
            .collect(Collectors.joining("."));
        return new Stmt.Import(keyword, pkgName, symbol);
    }

    private Stmt recordDeclaration(boolean isExported) {
        // Type Name
        int start = scanner.previous().span().start();
        scanner.consume(IDENTIFIER, "expected record type name.");
        var name = scanner.previous();

        // Record Fields
        scanner.consume(LEFT_PAREN, "expected '(' after record type name.");
        var recordFields = parameters(RIGHT_PAREN, true).stream()
            .map(Token::lexeme)
            .toList();

        if (recordFields.isEmpty()) {
            error(scanner.previous(), "expected at least one record parameter.");
        }

        // Type body
        scanner.consume(LEFT_BRACE, "expected '{' before type body.");

        List<Stmt.Function> staticMethods = new ArrayList<>();
        List<Stmt.Function> methods = new ArrayList<>();
        List<Stmt> staticInitializer = new ArrayList<>();

        while (!scanner.check(RIGHT_BRACE) && !scanner.isAtEnd()) {
            if (scanner.match(METHOD)) {
                methods.add(functionDeclaration(
                    FunctionType.METHOD, "method", false));
            } else if (scanner.match(STATIC)) {
                if (scanner.match(METHOD)) {
                    staticMethods.add(functionDeclaration(
                        FunctionType.STATIC_METHOD, "static method", false));
                } else {
                    scanner.consume(LEFT_BRACE,
                        "expected 'method' or '{' after 'static'.");
                    staticInitializer.addAll(block());
                }
            } else {
                scanner.advance();
                throw errorSync(scanner.previous(),
                    "expected method, static method, or static initializer.");
            }
        }

        scanner.consume(RIGHT_BRACE, "expected '}' after type body.");
        int end = scanner.previous().span().end();
        var span = source.span(start, end);

        return new Stmt.Record(name, isExported, span, recordFields,
            staticMethods, methods, staticInitializer);
    }

    private Stmt varDeclaration() {
        Token keyword = scanner.previous();
        Token patternToken = scanner.peek();
        ASTPattern pattern = pattern();

        if (pattern.getPattern() instanceof Pattern.Variable ||
            pattern.getPattern() instanceof Pattern.Wildcard
        ) {
            var name = scanner.previous();

            var initializer = scanner.match(EQUAL)
                ? expression()
                : new Expr.Null();

            scanner.consume(SEMICOLON, "expected ';' after variable declaration.");
            return new Stmt.Var(name, initializer);
        } else if (pattern.getPattern() instanceof Pattern.Constant) {
            throw errorSync(patternToken, "expected variable name.");
        } else if (pattern.getPattern() instanceof Pattern.Expression) {
            throw errorSync(patternToken, "expected variable name.");
        } else {
            if (pattern.getVariableTokens().isEmpty()) {
                error(scanner.previous(),
                    "'var' pattern must declare at least one variable.");
            }

            scanner.consume(EQUAL, "expected '=' after pattern.");
            var target = expression();
            scanner.consume(SEMICOLON, "expected ';' after target expression.");

            return new Stmt.VarPattern(keyword, pattern, target);
        }
    }

    private Stmt statement() {
        if (scanner.match(ASSERT)) return assertStatement();
        if (scanner.match(BREAK)) return breakStatement();
        if (scanner.match(CONTINUE)) return continueStatement();
        if (scanner.match(FOR)) return forStatement();
        if (scanner.match(FOREACH)) return foreachStatement();
        if (scanner.match(IF)) return ifStatement();
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

        scanner.consume(SEMICOLON, "expected ';' after assertion.");

        return new Stmt.Assert(keyword, condition, message);
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

        scanner.consume(RIGHT_BRACE, "expected '}' after block.");
        return statements;
    }

    private Stmt breakStatement() {
        Token keyword = scanner.previous();
        scanner.consume(SEMICOLON, "expected ';' after 'break'.");
        return new Stmt.Break(keyword);
    }

    private Stmt continueStatement() {
        Token keyword = scanner.previous();
        scanner.consume(SEMICOLON, "expected ';' after 'continue'.");
        return new Stmt.Continue(keyword);
    }

    private Stmt expressionStatement() {
        Expr value = expression();
        scanner.consume(SEMICOLON, "expected ';' after expression.");
        return new Stmt.Expression(value);
    }

    private Stmt forStatement() {
        var keyword = scanner.previous();
        var start = keyword.span().start();
        scanner.consume(LEFT_PAREN, "expected '(' after 'for'.");

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
        scanner.consume(SEMICOLON, "expected ';' after loop condition.");

        // Increment
        Expr incr = null;
        if (!scanner.check(RIGHT_PAREN)) {
            incr = expression();
        }
        scanner.consume(RIGHT_PAREN, "expected ')' after loop clauses.");

        // Body
        Stmt body = statement();

        // Wrap in a block so that the loop clauses have their local
        // scope.
        var end = scanner.previous().span().end();
        return new Stmt.Block(source.span(start, end),
            List.of(new Stmt.For(keyword, init, condition, incr, body)
        ));
    }

    private Stmt foreachStatement() {
        var keyword = scanner.previous();
        var start = keyword.span().start();
        scanner.consume(LEFT_PAREN, "expected '(' after 'foreach'.");
        var patternToken = scanner.peek();
        var pattern = pattern();

        // FIRST, Simple constants and interpolated expressions are invalid.
        if (pattern.getPattern() instanceof Pattern.Constant ||
            pattern.getPattern() instanceof Pattern.Expression
        ) {
            throw errorSync(patternToken, "expected loop variable name or pattern.");
        }

        // NEXT, One variable (a single wildcard is treated as variable name).
        if (pattern.getPattern() instanceof Pattern.Variable ||
            pattern.getPattern() instanceof Pattern.Wildcard
        ) {
            var varName = scanner.previous();
            scanner.consume(COLON, "expected ':' after loop variable name.");

            // List Expression
            Expr listExpr = expression();
            scanner.consume(RIGHT_PAREN, "expected ')' after loop expression.");

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

        // NEXT, A more complex pattern
        if (pattern.getVariableTokens().isEmpty()) {
            error(patternToken, "expected at least one variable in loop pattern.");
        }

        scanner.consume(COLON, "expected ':' after loop pattern.");

        // List Expression
        Expr listExpr = expression();
        scanner.consume(RIGHT_PAREN, "expected ')' after loop expression.");

        // Body
        var body = statement();
        var end = scanner.previous().span().end();

        return new Stmt.Block(
            source.span(start, end),
            List.of(
                new Stmt.ForEachBind(keyword, pattern, listExpr, body)
            ));
    }


    private Stmt ifStatement() {
        var keyword = scanner.previous();
        scanner.consume(LEFT_PAREN, "expected '(' after 'if'.");
        Expr condition = expression();
        scanner.consume(RIGHT_PAREN, "expected ')' after 'if' condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (scanner.match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(keyword, condition, thenBranch, elseBranch);
    }

    private Stmt matchStatement() {
        Token keyword = scanner.previous();
        var cases = new ArrayList<Stmt.MatchCase>();

        // FIRST, parse the head of the statement
        scanner.consume(LEFT_PAREN, "expected '(' after 'match'.");
        Expr target = expression();
        scanner.consume(RIGHT_PAREN, "expected ')' after 'match' expression.");
        scanner.consume(LEFT_BRACE, "expected '{' before 'match' body.");

        // NEXT, parse the cases.
        while (scanner.match(CASE)) {
            var caseKeyword = scanner.previous();
            var pattern = pattern();
            var guard = scanner.match(IF) ? expression() : null;
            scanner.consume(MINUS_GREATER, "expected '->' after case pattern.");
            var stmt = statement();
            cases.add(new Stmt.MatchCase(caseKeyword, pattern, guard, stmt));
        }

        if (cases.isEmpty()) {
            error(scanner.peek(), "expected at least one 'case' in 'match'.");
        }

        // NEXT, parse the default case, if it exists
        Stmt defCase = null;
        if (scanner.match(DEFAULT)) {
            scanner.consume(MINUS_GREATER, "expected '->' after 'default'.");
            defCase = statement();
        }

        // NEXT, complete the statement
        scanner.consume(RIGHT_BRACE, "expected '}' after 'match' body.");

        return new Stmt.Match(keyword, target, cases, defCase);
    }

    private Stmt returnStatement() {
        Token keyword = scanner.previous();
        Expr value = null;

        if (!scanner.check(SEMICOLON)) {
            value = expression();
        }

        scanner.consume(SEMICOLON, "expected ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt switchStatement() {
        Token keyword = scanner.previous();
        var cases = new ArrayList<Stmt.SwitchCase>();

        // FIRST, parse the head of the statement
        scanner.consume(LEFT_PAREN, "expected '(' after 'switch'.");
        Expr switchExpr = expression();
        scanner.consume(RIGHT_PAREN, "expected ')' after switch expression.");
        scanner.consume(LEFT_BRACE, "expected '{' before switch body.");

        // NEXT, parse the cases.
        while (scanner.match(CASE)) {
            var caseKeyword = scanner.previous();
            var values = new ArrayList<Expr>();

            // FIRST, parse the values
            values.add(expression());
            while (scanner.match(COMMA)) {
                values.add(expression());
            }
            scanner.consume(MINUS_GREATER, "expected '->' after case value.");
            var stmt = statement();
            cases.add(new Stmt.SwitchCase(caseKeyword, values, stmt));
        }

        if (cases.isEmpty()) {
            error(scanner.peek(), "expected at least one 'case' in switch.");
        }

        // NEXT, parse the default case, if it exists
        Stmt.SwitchDefault switchDefault = null;
        if (scanner.match(DEFAULT)) {
            var defaultKeyword = scanner.previous();
            scanner.consume(MINUS_GREATER, "expected '->' after 'default'.");
            var stmt = statement();
            switchDefault = new Stmt.SwitchDefault(defaultKeyword, stmt);
        }

        // NEXT, complete the statement
        scanner.consume(RIGHT_BRACE, "expected '}' after switch body.");

        return new Stmt.Switch(keyword, switchExpr, cases, switchDefault);
    }

    private Stmt throwStatement() {
        Token keyword = scanner.previous();
        Expr value = expression();

        scanner.consume(SEMICOLON, "expected ';' after thrown error.");
        return new Stmt.Throw(keyword, value);
    }

    private Stmt whileStatement() {
        var keyword = scanner.previous();
        scanner.consume(LEFT_PAREN, "expected '(' after 'while'.");
        Expr condition = expression();
        scanner.consume(RIGHT_PAREN, "expected ')' after condition.");
        Stmt body = statement();

        return new Stmt.While(keyword, condition, body);
    }

    //-------------------------------------------------------------------------
    // Expressions

    /**
     * Parses an Expr from the token stream.  Intentionally package-private,
     * so that it is available to EmbeddedParsers.
     * @return The Expr.
     */
    Expr expression() {
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

            error(op, "invalid assignment target.");
        }

        return target;
    }

    private Expr ternary() {
        var expr = or();

        if (scanner.match(QUESTION)) {
            var token = scanner.previous();
            var trueExpr = or();
            scanner.consume(COLON, "expected ':' after expression.");
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

        while (scanner.match(
            GREATER, GREATER_EQUAL, IN, LESS, LESS_EQUAL, NI, TILDE
        )) {
            Token operator = scanner.previous();
            if (operator.type() == TILDE) {
                ASTPattern right = pattern();
                expr = new Expr.Match(expr, operator, right);
            } else {
                Expr right = term();
                expr = new Expr.Binary(expr, operator, right);
            }
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

        error(op, "invalid '" + op.lexeme() + "' target.");
        return null;
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (scanner.match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (scanner.match(DOT)) {
                scanner.consume(IDENTIFIER, "expected property name after '.'.");
                var name = scanner.previous();
                expr = new Expr.PropGet(expr, name);
            } else if (scanner.match(LEFT_BRACKET)) {
                var bracket = scanner.previous();
                var index = expression();
                scanner.consume(RIGHT_BRACKET, "expected ']' after index.");
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

        scanner.consume(RIGHT_PAREN, "expected ')' after arguments.");
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

        // Property reference operator in instance methods.
        if (scanner.match(AT)) {
            Token keyword = scanner.previous();
            scanner.consume(IDENTIFIER, "expected property name.");
            var name = scanner.previous();
            var obj = new Expr.This(keyword);
            return new Expr.PropGet(obj, name);
        }

        if (scanner.match(RULESET)) return rulesetExpression();

        if (scanner.match(SUPER)) {
            Token keyword = scanner.previous();
            scanner.consume(DOT, "expected '.' after 'super'.");
            scanner.consume(IDENTIFIER,
                "expected superclass method name.");
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
                    false, token, parameters, body, span);
            return new Expr.Lambda(decl);
        }

        if (scanner.match(LEFT_PAREN)) {
            Expr expr = expression();
            scanner.consume(RIGHT_PAREN, "expected ')' after expression.");
            return new Expr.Grouping(expr);
        }

        // Other literals
        if (scanner.match(LEFT_BRACKET)) return listLiteral();
        if (scanner.match(LEFT_BRACE)) return setOrMapLiteral();

        throw errorSync(scanner.peek(), "expected expression.");
    }

    private Expr listLiteral() {
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

        scanner.consume(RIGHT_BRACKET, "expected ']' after list items.");

        return new Expr.ListLiteral(bracket, list);
    }

    private Expr setOrMapLiteral() {
        var brace = scanner.previous();

        // FIRST, look for empty set
        if (scanner.match(RIGHT_BRACE)) {
            return new Expr.SetLiteral(brace, List.of());
        }

        // NEXT, look for empty map
        if (scanner.match(COLON)) {
            scanner.consume(RIGHT_BRACE,
                "expected '}' after ':' in empty map literal.");
            return new Expr.MapLiteral(brace, List.of());
        }

        // NEXT, get first expression
        var first = expression();

        // NEXT, is it a set or a map?
        if (scanner.match(COLON)) {
            return mapLiteral(brace, first);
        } else {
            return setLiteral(brace, first);
        }
    }

    private Expr setLiteral(Token brace, Expr first) {
        var list = new ArrayList<Expr>();
        list.add(first);

        if (!scanner.check(RIGHT_BRACE)) {
            while (scanner.match(COMMA)) {
                // Allow trailing comma
                if (scanner.check(RIGHT_BRACE)) break;
                list.add(expression());
            }
        }

        scanner.consume(RIGHT_BRACE, "expected '}' after set items.");

        return new Expr.SetLiteral(brace, list);
    }

    private Expr mapLiteral(Token brace, Expr first) {
        var entries = new ArrayList<Expr>();
        entries.add(first);
        entries.add(expression()); // The colon was already parsed

        if (!scanner.check(RIGHT_BRACE)) {
            while (scanner.match(COMMA)) {
                // Allow trailing comma
                if (scanner.check(RIGHT_BRACE)) break;
                entries.add(expression());
                scanner.consume(COLON, "expected ':' after map key.");
                entries.add(expression());
            }
        }

        scanner.consume(RIGHT_BRACE, "expected '}' after map items.");

        return new Expr.MapLiteral(brace, entries);
    }

    //-------------------------------------------------------------------------
    // Patterns

    private ASTPattern pattern() {
        return new PatternParser(this).parse();
    }

    //-------------------------------------------------------------------------
    // Nero Rule Sets

    /**
     * Parses the source as embedded in a script, program, attempting to detect
     * as many meaningful errors as possible.  Errors are reported via the
     * parser's error reporter.  If errors were reported, the result of
     * this method should be ignored.
     * @return The parsed rule set.
     */
    public Expr.RuleSet rulesetExpression() {
        var keyword = scanner.previous();
        scanner.consume(LEFT_BRACE, "expected '{' after 'ruleset'.");

        var ruleSet = new NeroParser(this, NeroParser.Mode.EMBEDDED).parse();
        return new Expr.RuleSet(keyword, ruleSet);
    }

    /**
     * Parses the source as a standalone Nero program, attempting to detect
     * as many meaningful errors as possible.  Errors are reported via the
     * parser's error reporter.  If errors were reported, the result of
     * this method should be ignored.
     * @return The parsed rule set.
     */
    public NeroRuleSet parseNero() {
        this.scanner = new Scanner(source, this::errorInScanner);
        scanner.prime();
        return new NeroParser(this, NeroParser.Mode.STANDALONE).parse();
    }

    //-------------------------------------------------------------------------
    // Primitives

    /**
     * Returns the scanner, for use by EmbeddedParsers.
     * Intentionally package-private.
     * @return The scanner
     */
    Scanner scanner() {
        return scanner;
    }

    /**
     * Reports the error with no synchronization.  Use this for semantic
     * errors where the syntax is OK.  Intentionally package-private.
     * @param token The token at which the error was detected.
     * @param message The error message.
     */
    void error(Token token, String message) {
        var msg = token.type() == TokenType.EOF
            ? "error at end, " + message
            : "error at '" + token.lexeme() + "', " + message;
        reporter.reportError(new Trace(token.span(), msg),
            token.type() == TokenType.EOF);
    }

    /**
     * Reports the error, returning an ErrorSync exception to trigger
     * synchronization.  The exception is thrown by the caller.
     * Use this for syntax errors where the parser would get confused
     * without synchronization.  Intentionally package-private.
     * @param token The token at which the error was detected.
     * @param message The error message.
     * @return The exception
     */
    ErrorSync errorSync(Token token, String message) {
        error(token, message);
        return new ErrorSync(message);
    }

    /**
     * Sets the synchronizing flag.  For use by EmbeddedParsers that do
     * their own synchronization.  Intentionally package-private.
     * @param flag true or false
     */
    void setSynchronizing(boolean flag) {
        this.synchronizing = flag;
    }

    // Discard tokens until we are at the beginning of the next statement.
    private void synchronize() {
        try {
            synchronizing = true;

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
        } finally {
            synchronizing = false;
        }
    }

    /**
     * An exception used to synchronize errors.  Intentionally package-private.
     */
    static class ErrorSync extends RuntimeException {
        ErrorSync(String message) {
            super(message);
        }
    }
}
