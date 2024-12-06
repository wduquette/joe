package com.wjduquette.joe.bert;

import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.Trace;

import static com.wjduquette.joe.bert.TokenType.*;

import java.util.ArrayList;
import java.util.List;

class Compiler {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final List<Trace> errors = new ArrayList<>();
    private final Parser parser = new Parser();
    private Scanner scanner;
    private Chunk compilingChunk;
    private final Disassembler disassembler = new Disassembler();

    //-------------------------------------------------------------------------
    // Constructor

    Compiler() {
        populateRulesTable();
    }

    //-------------------------------------------------------------------------
    // Compilation

    public void compile(String source, Chunk chunk) {
        this.scanner = new Scanner("*script*", source, errors::add);
        this.compilingChunk = chunk;

        errors.clear();
        parser.hadError = false;
        parser.panicMode = false;

        advance();
        expression();
        consume(EOF, "Expected end of expression.");
        endCompiler();

        if (!errors.isEmpty()) {
            throw new SyntaxError("Error while compiling script", errors);
        }
    }

    private Chunk currentChunk() {
        return compilingChunk;
    }

    private void endCompiler() {
        emitReturn();
        if (!parser.hadError && Bert.isDebug()) {
            Bert.println(disassembler.disassemble("*script*", currentChunk()));
        }
    }

    //-------------------------------------------------------------------------
    // Parser

    private void expression() {
        parsePrecedence(Level.ASSIGNMENT);
    }

    void grouping() {
        expression();
        consume(RIGHT_PAREN, "Expected ')' after expression.");
    }

    void binary() {
        var op = parser.previous.type();
        var rule = getRule(op);
        parsePrecedence(rule.level + 1);

        switch (op) {
            case PLUS ->  emit(Opcode.ADD);
            case MINUS -> emit(Opcode.SUB);
            case STAR  -> emit(Opcode.MUL);
            case SLASH -> emit(Opcode.DIV);
            default -> throw new IllegalStateException(
                "Unexpected operator: " + op);
        }
    }

    void unary() {
        var op = parser.previous.type();

        // Compile the operand
        parsePrecedence(Level.UNARY);

        // Emit the instruction
        switch (op) {
            case MINUS -> emit(Opcode.NEGATE);
            default -> throw new IllegalStateException(
                "Unexpected operator: " + op);
        }
    }
    void number() {
        emitConstant(parser.previous.literal());
    }

    private void parsePrecedence(int level) {
        advance();
        var prefixRule = getRule(parser.previous.type()).prefix;

        if (prefixRule == null) {
            error("Expected expression.");
            return;
        }

        prefixRule.parse();

        while (level <= getRule(parser.current.type()).level) {
            advance();
            var infixRule = getRule(parser.previous.type()).infix;
            infixRule.parse();
        }
    }

    //-------------------------------------------------------------------------
    // Parsing Tools

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

    private void emitReturn() {
        emit(Opcode.RETURN);
    }

    private void emit(char value) {
        currentChunk().write(value, parser.previous.line());
    }

    private void emit(char value1, char value2) {
        emit(value1);
        emit(value2);
    }

    private void emit(char value1, char value2, char value3) {
        emit(value1);
        emit(value2);
        emit(value3);
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
        void parse();
    }

    private record ParseRule(
        ParseFunction prefix,
        ParseFunction infix,
        int level
    ) {}

    //-------------------------------------------------------------------------
    // Parser Rules

    private final ParseRule[] rules = new ParseRule[TokenType.values().length];

    private void populateRulesTable() {
        rule(LEFT_PAREN,      this::grouping, null,         Level.NONE);
        rule(RIGHT_PAREN,     null,           null,         Level.NONE);
        rule(LEFT_BRACE,      null,           null,         Level.NONE);
        rule(RIGHT_BRACE,     null,           null,         Level.NONE);
        rule(AT,              null,           null,         Level.NONE);
        rule(BACK_SLASH,      null,           null,         Level.NONE);
        rule(COLON,           null,           null,         Level.NONE);
        rule(COMMA,           null,           null,         Level.NONE);
        rule(DOT,             null,           null,         Level.NONE);
        rule(MINUS,           this::unary,    this::binary, Level.TERM);
        rule(PLUS,            null,           this::binary, Level.TERM);
        rule(QUESTION,        null,           null,         Level.TERM);
        rule(SEMICOLON,       null,           null,         Level.NONE);
        rule(SLASH,           null,           this::binary, Level.FACTOR);
        rule(STAR,            null,           this::binary, Level.FACTOR);
        rule(AND,             null,           null,         Level.NONE);
        rule(BANG,            null,           null,         Level.NONE);
        rule(BANG_EQUAL,      null,           null,         Level.NONE);
        rule(EQUAL,           null,           null,         Level.NONE);
        rule(EQUAL_EQUAL,     null,           null,         Level.NONE);
        rule(GREATER,         null,           null,         Level.NONE);
        rule(GREATER_EQUAL,   null,           null,         Level.NONE);
        rule(MINUS_EQUAL,     null,           null,         Level.NONE);
        rule(MINUS_GREATER,   null,           null,         Level.NONE);
        rule(MINUS_MINUS,     null,           null,         Level.NONE);
        rule(LESS,            null,           null,         Level.NONE);
        rule(LESS_EQUAL,      null,           null,         Level.NONE);
        rule(OR,              null,           null,         Level.NONE);
        rule(PLUS_EQUAL,      null,           null,         Level.NONE);
        rule(PLUS_PLUS,       null,           null,         Level.NONE);
        rule(SLASH_EQUAL,     null,           null,         Level.NONE);
        rule(STAR_EQUAL,      null,           null,         Level.NONE);
        rule(IDENTIFIER,      null,           null,         Level.NONE);
        rule(STRING,          null,           null,         Level.NONE);
        rule(NUMBER,          this::number,   null,         Level.NONE);
        rule(KEYWORD,         null,           null,         Level.NONE);
        rule(ASSERT,          null,           null,         Level.NONE);
        rule(BREAK,           null,           null,         Level.NONE);
        rule(CASE,            null,           null,         Level.NONE);
        rule(CLASS,           null,           null,         Level.NONE);
        rule(CONTINUE,        null,           null,         Level.NONE);
        rule(DEFAULT,         null,           null,         Level.NONE);
        rule(ELSE,            null,           null,         Level.NONE);
        rule(EXTENDS,         null,           null,         Level.NONE);
        rule(FALSE,           null,           null,         Level.NONE);
        rule(FOR,             null,           null,         Level.NONE);
        rule(FOREACH,         null,           null,         Level.NONE);
        rule(FUNCTION,        null,           null,         Level.NONE);
        rule(IF,              null,           null,         Level.NONE);
        rule(IN,              null,           null,         Level.NONE);
        rule(METHOD,          null,           null,         Level.NONE);
        rule(NI,              null,           null,         Level.NONE);
        rule(NULL,            null,           null,         Level.NONE);
        rule(RETURN,          null,           null,         Level.NONE);
        rule(STATIC,          null,           null,         Level.NONE);
        rule(SUPER,           null,           null,         Level.NONE);
        rule(SWITCH,          null,           null,         Level.NONE);
        rule(THIS,            null,           null,         Level.NONE);
        rule(THROW,           null,           null,         Level.NONE);
        rule(TRUE,            null,           null,         Level.NONE);
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
