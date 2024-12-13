package com.wjduquette.joe.bert;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.Trace;

import static com.wjduquette.joe.bert.TokenType.*;

import java.util.ArrayList;
import java.util.List;

class Compiler {
    public static final int MAX_LOCALS = 256;
    public static final int MAX_PARAMETERS = 255;

    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    private final List<Trace> errors = new ArrayList<>();
    private SourceBuffer buffer;
    private Scanner scanner;
    private final Parser parser = new Parser();
    private FunctionCompiler current = null;

    // Used for debugging/dumping
    private transient Disassembler disassembler;
    private transient StringBuilder dump = null;


    //-------------------------------------------------------------------------
    // Constructor

    Compiler(Joe joe) {
        this.joe = joe;
        populateRulesTable();
    }

    //-------------------------------------------------------------------------
    // Compilation

    public Function compile(String scriptName, String source) {
        buffer = new SourceBuffer(scriptName, source);
        scanner = new Scanner(buffer, errors::add);
        current = new FunctionCompiler(null, FunctionType.SCRIPT, buffer);
        current.chunk.span = buffer.all();

        errors.clear();
        parser.hadError = false;
        parser.panicMode = false;

        advance();
        while (!match(EOF)) {
            declaration();
        }

        var function = endFunction();

        if (!errors.isEmpty()) {
            throw new SyntaxError("Error while compiling script", errors);
        }

        return function;
    }

    public String dump(String scriptName, String source) {
        dump = new StringBuilder();
        disassembler = new Disassembler(joe);

        compile(scriptName, source);

        var output = dump.toString();
        dump = null;
        disassembler = null;
        return output;
    }

    private Chunk currentChunk() {
        return current.chunk;
    }

    private Function endFunction() {
        emitReturn();
        var function = new Function(currentChunk());
        if (dump != null) {
            dump.append(disassembler.disassemble(function)).append("\n");
        }
        current = current.enclosing;
        return function;
    }

    //-------------------------------------------------------------------------
    // Parser: Statements

    private void declaration() {
        if (match(FUNCTION)) {
            functionDeclaration();
        } else if (match(VAR)) {
            varDeclaration();
        } else {
            statement();
        }

        if (parser.panicMode) synchronize();
    }

    private void functionDeclaration() {
        int start = parser.previous.span().start();
        var global = parseVariable("Expected function name.");
        markVariableInitialized();
        function(start, FunctionType.FUNCTION);
        defineVariable(global);
    }

    private void function(int start, FunctionType type) {
        this.current = new FunctionCompiler(current, type, buffer);
        beginScope();

        consume(LEFT_PAREN, "Expected '(' after function name.");
        if (!check(RIGHT_PAREN)) {
            ++current.chunk.arity;
            if (current.chunk.arity > MAX_PARAMETERS) {
                errorAtCurrent(
                    "Can't have more than " + MAX_PARAMETERS + "parameters.");
            }
            var constant = parseVariable("Expected parameter name.");
            defineVariable(constant);
        }
        consume(RIGHT_PAREN, "Expected ')' after function name.");
        consume(LEFT_BRACE, "Expected '{' before function body.");
        block();
        var end = parser.previous.span().end();
        currentChunk().span = buffer.span(start, end);

        var function = endFunction();
        emit(Opcode.CONST, currentChunk().addConstant(function));
    }

    private void varDeclaration() {
        char global = parseVariable("Expected variable name.");

        if (match(EQUAL)) {
            expression();
        } else {
            emit(Opcode.NULL);
        }
        consume(SEMICOLON, "Expected ';' after variable declaration.");
        defineVariable(global);
    }

    private void synchronize() {
        parser.panicMode = false;

        while (parser.current.type() != EOF) {
            if (parser.previous.type() == SEMICOLON) return;
            switch (parser.current.type()) {
                case ASSERT:
                case BREAK:
                case CLASS:
                case CONTINUE:
                case FOR:
                case FOREACH:
                case FUNCTION:
                case IF:
                case METHOD:
                case PRINT:
                case RETURN:
                case SWITCH:
                case THROW:
                case VAR:
                case WHILE:
                    return;

                default: // Do nothing.
            }

            advance();
        }
    }

    private void statement() {
        if (match(IF)) {
            ifStatement();
        } else if (match(FOR)) {
            forStatement();
        } else if (match(PRINT)) {
            printStatement();
        } else if (match(RETURN)) {
            returnStatement();
        } else if (match(WHILE)) {
            whileStatement();
        } else if (match(LEFT_BRACE)) {
            beginScope();
            block();
            endScope();
        } else {
            expressionStatement();
        }
    }

    private void block() {
        while (!check(RIGHT_BRACE) && !check(EOF)) {
            declaration();
        }
        consume(RIGHT_BRACE, "Expected '}' after block.");
    }

    private void expressionStatement() {
        expression();
        consume(SEMICOLON, "Expected ';' after expression.");

        // Normal statements should not leave anything on the stack, so we
        // pop it.  But if this is the last statement in the script, we
        // want to return its value.
        if (parser.current.type() == EOF) {
            emit(Opcode.RETURN);
        } else {
            emit(Opcode.POP);
        }
    }

    private void forStatement() {
        beginScope();
        consume(LEFT_PAREN, "Expected '(' after 'for'.");

        // Initializer
        if (match(SEMICOLON)) {
            // No initializer
        } else if (match(VAR)) {
            varDeclaration();
        } else {
            expressionStatement();
        }

        // Condition
        int loopStart = currentChunk().codeSize();
        int exitJump = -1;
        if (!match(SEMICOLON)) {
            expression();
            consume(SEMICOLON, "Expected ';' after loop condition.");

            // Jump out of the loop if the condition is false.
            exitJump = emitJump(Opcode.JIF);
        }

        if (!match(RIGHT_PAREN)) {
            int bodyJump = emitJump(Opcode.JUMP);
            int incrementStart = currentChunk().codeSize();
            expression();
            emit(Opcode.POP);
            consume(RIGHT_PAREN, "Expected ')' after 'for' clauses.");
            emitLoop(loopStart);
            loopStart = incrementStart;
            patchJump(bodyJump);
        }

        statement();
        emitLoop(loopStart);
        if (exitJump != -1) {
            patchJump(exitJump);
        }
        endScope();
    }

    private void ifStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'if'.");
        expression();
        consume(RIGHT_PAREN, "Expected '(' after condition.");

        int thenJump = emitJump(Opcode.JIF);
        statement();
        int elseJump = emitJump(Opcode.JUMP);
        patchJump(thenJump);

        if (match(ELSE)) statement();
        patchJump(elseJump);
    }

    private void returnStatement() {
        if (match(SEMICOLON)) {
            emitReturn();
        } else {
            expression();
            consume(SEMICOLON, "Expected ';' after return value.");
            emit(Opcode.RETURN);
        }
    }

    private void whileStatement() {
        int loopStart = currentChunk().codeSize();
        consume(LEFT_PAREN, "Expected '(' after 'while'.");
        expression();
        consume(RIGHT_PAREN, "Expected '(' after condition.");

        int exitJump = emitJump(Opcode.JIF);
        statement();
        emitLoop(loopStart);

        patchJump(exitJump);
    }

    private void printStatement() {
        expression();
        consume(SEMICOLON, "Expected ';' after value.");
        emit(Opcode.PRINT);
    }

    //-------------------------------------------------------------------------
    // Parser: Expressions

    private void expression() {
        parsePrecedence(Level.ASSIGNMENT);
    }

    void grouping(boolean canAssign) {
        expression();
        consume(RIGHT_PAREN, "Expected ')' after expression.");
    }

    void binary(boolean canAssign) {
        var op = parser.previous.type();
        var rule = getRule(op);
        parsePrecedence(rule.level + 1);

        switch (op) {
            case BANG_EQUAL -> emit(Opcode.NE);
            case EQUAL_EQUAL -> emit(Opcode.EQ);
            case GREATER -> emit(Opcode.GT);
            case GREATER_EQUAL -> emit(Opcode.GE);
            case LESS -> emit(Opcode.LT);
            case LESS_EQUAL -> emit(Opcode.LE);
            case PLUS ->  emit(Opcode.ADD);
            case MINUS -> emit(Opcode.SUB);
            case STAR  -> emit(Opcode.MUL);
            case SLASH -> emit(Opcode.DIV);
            default -> throw new IllegalStateException(
                "Unexpected operator: " + op);
        }
    }

    void and(boolean canAssign) {
        // On false, the tested value becomes the value of the expression.
        int endJump = emitJump(Opcode.JIFKEEP);
        emit(Opcode.POP);
        parsePrecedence(Level.AND);
        patchJump(endJump);
    }

    void or(boolean canAssign) {
        // On true, the tested value becomes the value of the expression.
        int endJump = emitJump(Opcode.JITKEEP);
        emit(Opcode.POP);
        parsePrecedence(Level.OR);
        patchJump(endJump);
    }

    private void unary(boolean canAssign) {
        var op = parser.previous.type();

        // Compile the operand
        parsePrecedence(Level.UNARY);

        // Emit the instruction
        switch (op) {
            case BANG -> emit(Opcode.NOT);
            case MINUS -> emit(Opcode.NEGATE);
            default -> throw new IllegalStateException(
                "Unexpected operator: " + op);
        }
    }

    private void variable(boolean canAssign) {
        namedVariable(parser.previous, canAssign);
    }

    private void literal(boolean canAssign) {
        emitConstant(parser.previous.literal());
    }

    private void symbol(boolean canAssign) {
        switch (parser.previous.type()) {
            case FALSE -> emit(Opcode.FALSE);
            case NULL -> emit(Opcode.NULL);
            case TRUE -> emit(Opcode.TRUE);
            default -> throw new IllegalStateException(
                "Unexpected literal: " + parser.previous.type());
        }
    }

    private void call(boolean canAssign) {
        var argCount = argumentList();
        emit(Opcode.CALL, argCount);
    }

    private char argumentList() {
        char count = 0;

        if (!check(RIGHT_PAREN)) {
            do {
                expression();
                count++;
                if (count > MAX_PARAMETERS) {
                    error("Can't have more than 255 arguments.");
                }
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expected ')' after arguments.");
        return count;
    }

    private void parsePrecedence(int level) {
        advance();
        var prefixRule = getRule(parser.previous.type()).prefix;

        if (prefixRule == null) {
            error("Expected expression.");
            return;
        }

        var canAssign = level <= Level.ASSIGNMENT;
        prefixRule.parse(canAssign);

        while (level <= getRule(parser.current.type()).level) {
            advance();
            var infixRule = getRule(parser.previous.type()).infix;
            infixRule.parse(canAssign);
        }

        if (canAssign && match(EQUAL)) {
            error("Invalid assignment target.");
        }
    }

    //-------------------------------------------------------------------------
    // Variable Management
    //
    // TODO: Refactor
    // See what's here that can be folded into FunctionCompiler or
    // Local or whatever.  As it is, it's confusing.

    // Consumes an IDENTIFIER and returns the constant index
    // for the identifier's name constant.
    private char parseVariable(String errorMessage) {
        consume(IDENTIFIER, errorMessage);
        declareVariable();
        if (current.scopeDepth > 0) return 0;       // Local
        return identifierConstant(parser.previous); // Global
    }

    // Adds a string constant to the current chunk's
    // constants table for the given identifier.
    private char identifierConstant(Token name) {
        return currentChunk().addConstant(name.lexeme());
    }

    // Emits the instruction to define the variable.
    private void defineVariable(char global) {
        if (current.scopeDepth > 0) {
            // Local
            markVariableInitialized();
            return;
        }
        emit(Opcode.GLODEF, global);            // Global
    }

    private void namedVariable(Token name, boolean canAssign) {
        char getOp;
        char setOp;

        int arg = resolveLocal(current, name);

        if (arg != -1) {
            getOp = Opcode.LOCGET;
            setOp = Opcode.LOCSET;
        } else {
            arg = identifierConstant(name);
            getOp = Opcode.GLOGET;
            setOp = Opcode.GLOSET;
        }
        if (canAssign && match(EQUAL)) {
            expression();
            emit(setOp, (char)arg);
        } else {
            emit(getOp, (char)arg);
        }
    }

    private void declareVariable() {
        if (current.scopeDepth == 0) return; // Global
        var name = parser.previous;

        // Check for duplicate declarations in current scope.
        for (var i = current.localCount - 1; i >= 0; i--) {
            var local = current.locals[i];

            // Stop checking once we get to a lower scope depth.
            if (local.depth != -1 && local.depth < current.scopeDepth) {
                break;
            }

            if (name.lexeme().equals(local.name.lexeme())) {
                error("Duplicate variable declaration in this scope.");
            }
        }

        addLocal(name);
    }

    private void addLocal(Token name) {
        if (current.localCount == MAX_LOCALS) {
            error("Too many local variables in function.");
        }
        current.locals[current.localCount++] =
            new Local(name);
    }

    // Marks local variables "initialized", so that they can be
    // referred to in expressions.  This is a no-op for global variables.
    private void markVariableInitialized() {
        if (current.scopeDepth == 0) return;
        current.locals[current.localCount - 1].depth
            = current.scopeDepth;
    }

    private int resolveLocal(FunctionCompiler compiler, Token name) {
        for (var i = compiler.localCount - 1; i >= 0; i--) {
            var local = compiler.locals[i];
            if (name.lexeme().equals(local.name.lexeme())) {
                if (local.depth == -1) {
                    error("Can't read local variable in its own initializer.");
                }
                return i;
            }
        }
        return -1;
    }

    private void beginScope() {
        current.scopeDepth++;
    }

    private void endScope() {
        current.scopeDepth--;

        while (current.localCount > 0
            && current.locals[current.localCount - 1].depth > current.scopeDepth)
        {
            emit(Opcode.POP);
            current.localCount--;
        }
    }

    //-------------------------------------------------------------------------
    // Parsing Tools

    private boolean match(TokenType type) {
        if (!check(type)) return false;
        advance();
        return true;
    }

    private boolean check(TokenType type) {
        return parser.current.type() == type;
    }

    private void advance() {
        parser.previous = parser.current;

        for (;;) {
            parser.current = scanner.scanToken();
            if (parser.current.type() != ERROR) break;

            // The error has already been reported.
            parser.hadError = true;
        }
    }

    private void consume(TokenType type, String message) {
        if (parser.current.type() == type) {
            advance();
            return;
        }

        errorAtCurrent(message);
    }

    private void error(String message) {
        errorAt(parser.previous, message);
    }

    private void errorAtCurrent(String message) {
        errorAt(parser.current, message);
    }

    private void errorAt(Token token, String message) {
        if (parser.panicMode) return;
        parser.panicMode = true;
        errors.add(new Trace(token.span(), message));
        parser.hadError = true;
    }

    //-------------------------------------------------------------------------
    // Code Generation

    private void emitConstant(Object value) {
        emit(Opcode.CONST, currentChunk().addConstant(value));
    }

    private int emitJump(char opcode) {
        emit(opcode);
        emit(Character.MAX_VALUE);
        return currentChunk().codeSize() - 1;
    }

    private void emitLoop(int loopStart) {
        emit(Opcode.LOOP);
        int offset = currentChunk().codeSize() - loopStart + 1;
        if (offset > Character.MAX_VALUE) error("Loop body too large.");
        emit((char)offset);
    }

    private void patchJump(int offset) {
        // -1 to adjust for the bytecode for the jump offset itself.
        int jump = currentChunk().codeSize() - offset - 1;

        if (jump > Character.MAX_VALUE) {
            error("Too much code to jump over.");
        }

        currentChunk().setCode(offset, (char)jump);
    }

    private void emitReturn() {
        emit(Opcode.NULL);
        emit(Opcode.RETURN);
    }

    private void emit(char value) {
        currentChunk().write(value, parser.previous.line());
    }

    private void emit(char value1, char value2) {
        emit(value1);
        emit(value2);
    }

    //-------------------------------------------------------------------------
    // Helper Classes

    private static class Parser {
        Token current = null;
        Token previous = null;
        boolean hadError = false;
        boolean panicMode = false;
    }

    // Precedence levels
    @SuppressWarnings("unused")
    private static class Level {
        private Level() {} // Not Instantiable
        private final static int NONE         = 0;
        private final static int ASSIGNMENT   = 1;  // =
        private final static int OR           = 2;  // ||
        private final static int AND          = 3;  // &&
        private final static int EQUALITY     = 4;  // == !=
        private final static int COMPARISON   = 5;  // < > <= >=
        private final static int TERM         = 6;  // + -
        private final static int FACTOR       = 7;  // * /
        private final static int UNARY        = 8;  // ! -
        private final static int CALL         = 9;  // . ()
        private final static int PRIMARY      = 10;
    }

    private interface ParseFunction {
        void parse(boolean canAssign);
    }

    private record ParseRule(
        ParseFunction prefix,
        ParseFunction infix,
        int level
    ) {}

    private static class Local {
        final Token name;
        int depth = - 1;

        Local(Token name) {
            this.name = name;
        }
    }

    private class FunctionCompiler {
        final FunctionCompiler enclosing;
        final Chunk chunk;
        final Local[] locals = new Local[MAX_LOCALS];
        int localCount = 0;
        int scopeDepth = 0;

        FunctionCompiler(
            FunctionCompiler enclosing,
            FunctionType type,
            SourceBuffer source
        ) {
            this.enclosing = enclosing;
            this.chunk = new Chunk();
            chunk.name = type == FunctionType.SCRIPT
                ? "*script*"
                : parser.previous.lexeme();
            chunk.type = type;
            chunk.source = source;

            // Every function has an implicit stack slot for the VM's own use.
            // For methods, this slot will be filled by the instance.
            var local = new Local(Token.synthetic(""));
            local.depth = 0;
            locals[localCount++] = local;
        }
    }

    //-------------------------------------------------------------------------
    // Parser Rules

    private final ParseRule[] rules = new ParseRule[TokenType.values().length];

    // Tokens are in the same order as in TokenType
    private void populateRulesTable() {
        //                                                  Infix
        //   Token            Prefix          Infix         precedence
        //   ---------------- --------------- ------------- ----------------
        //   Single Character
        rule(LEFT_PAREN,      this::grouping, this::call,   Level.CALL);
        rule(RIGHT_PAREN,     null,           null,         Level.NONE);
        rule(LEFT_BRACE,      null,           null,         Level.NONE);
        rule(RIGHT_BRACE,     null,           null,         Level.NONE);
        rule(AT,              null,           null,         Level.NONE);
        rule(BACK_SLASH,      null,           null,         Level.NONE);
        rule(COLON,           null,           null,         Level.NONE);
        rule(COMMA,           null,           null,         Level.NONE);
        rule(DOT,             null,           null,         Level.NONE);
        rule(QUESTION,        null,           null,         Level.TERM);
        rule(SEMICOLON,       null,           null,         Level.NONE);
        //   One or two character
        rule(AND,             null,           this::and,    Level.AND);
        rule(BANG,            this::unary,    null,         Level.NONE);
        rule(BANG_EQUAL,      null,           this::binary, Level.EQUALITY);
        rule(EQUAL,           null,           null,         Level.NONE);
        rule(EQUAL_EQUAL,     null,           this::binary, Level.EQUALITY);
        rule(GREATER,         null,           this::binary, Level.COMPARISON);
        rule(GREATER_EQUAL,   null,           this::binary, Level.COMPARISON);
        rule(LESS,            null,           this::binary, Level.COMPARISON);
        rule(LESS_EQUAL,      null,           this::binary, Level.COMPARISON);
        rule(MINUS,           this::unary,    this::binary, Level.TERM);
        rule(MINUS_EQUAL,     null,           null,         Level.NONE);
        rule(MINUS_GREATER,   null,           null,         Level.NONE);
        rule(MINUS_MINUS,     null,           null,         Level.NONE);
        rule(OR,              null,           this::or,     Level.OR);
        rule(PLUS,            null,           this::binary, Level.TERM);
        rule(PLUS_EQUAL,      null,           null,         Level.NONE);
        rule(PLUS_PLUS,       null,           null,         Level.NONE);
        rule(SLASH,           null,           this::binary, Level.FACTOR);
        rule(SLASH_EQUAL,     null,           null,         Level.NONE);
        rule(STAR,            null,           this::binary, Level.FACTOR);
        rule(STAR_EQUAL,      null,           null,         Level.NONE);
        //   Literals
        rule(IDENTIFIER,      this::variable, null,         Level.NONE);
        rule(STRING,          this::literal,  null,         Level.NONE);
        rule(NUMBER,          this::literal,  null,         Level.NONE);
        rule(KEYWORD,         null,           null,         Level.NONE);
        //   Reserved words
        rule(ASSERT,          null,           null,         Level.NONE);
        rule(BREAK,           null,           null,         Level.NONE);
        rule(CASE,            null,           null,         Level.NONE);
        rule(CLASS,           null,           null,         Level.NONE);
        rule(CONTINUE,        null,           null,         Level.NONE);
        rule(DEFAULT,         null,           null,         Level.NONE);
        rule(ELSE,            null,           null,         Level.NONE);
        rule(EXTENDS,         null,           null,         Level.NONE);
        rule(FALSE,           this::symbol,   null,         Level.NONE);
        rule(FOR,             null,           null,         Level.NONE);
        rule(FOREACH,         null,           null,         Level.NONE);
        rule(FUNCTION,        null,           null,         Level.NONE);
        rule(IF,              null,           null,         Level.NONE);
        rule(IN,              null,           null,         Level.NONE);
        rule(METHOD,          null,           null,         Level.NONE);
        rule(NI,              null,           null,         Level.NONE);
        rule(NULL,            this::symbol,   null,         Level.NONE);
        rule(RETURN,          null,           null,         Level.NONE);
        rule(STATIC,          null,           null,         Level.NONE);
        rule(SUPER,           null,           null,         Level.NONE);
        rule(SWITCH,          null,           null,         Level.NONE);
        rule(THIS,            null,           null,         Level.NONE);
        rule(THROW,           null,           null,         Level.NONE);
        rule(TRUE,            this::symbol,   null,         Level.NONE);
        rule(VAR,             null,           null,         Level.NONE);
        rule(WHILE,           null,           null,         Level.NONE);
        rule(PRINT,           null,           null,         Level.NONE);
        rule(ERROR,           null,           null,         Level.NONE);
        rule(EOF,             null,           null,         Level.NONE);

        for (var type : TokenType.values()) {
            if (rules[type.ordinal()] == null) {
                throw new IllegalStateException(
                    "Missing ParseRule for TokenType." + type);
            }
        }
    }

    private void rule(
        TokenType type,
        ParseFunction prefix,
        ParseFunction infix,
        int level
    ) {
        rules[type.ordinal()] = new ParseRule(prefix, infix, level);
    }

    private ParseRule getRule(TokenType type) {
        return rules[type.ordinal()];
    }
}
