package com.wjduquette.joe.scanner;

/**
 * The {@link Token} types that can be returned by the {@link Scanner}.
 */
public enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    LEFT_BRACKET, RIGHT_BRACKET,
    AT, BACK_SLASH, COLON, COMMA, DOLLAR, DOT, QUESTION,
    SEMICOLON,

    // One or two character tokens.
    AND,
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    MINUS, MINUS_EQUAL, MINUS_GREATER, MINUS_MINUS,
    LESS, LESS_EQUAL,
    OR,
    PLUS, PLUS_EQUAL, PLUS_PLUS,
    SLASH, SLASH_EQUAL,
    STAR, STAR_EQUAL,

    // Literals.
    IDENTIFIER, STRING, NUMBER, KEYWORD,

    // Reserved Words.
    ASSERT, BREAK, CASE, CLASS, CONTINUE, DEFAULT, ELSE, EXTENDS,
    FALSE, FOR, FOREACH, FUNCTION, IF, IN, LET, MATCH, METHOD, NI, NULL,
    RECORD, RETURN, STATIC, SUPER, SWITCH, THIS, THROW, TRUE, VAR, WHILE,

    ERROR,
    EOF
}
