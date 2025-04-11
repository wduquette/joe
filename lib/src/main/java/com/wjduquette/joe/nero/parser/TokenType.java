package com.wjduquette.joe.nero.parser;

/** Nero Token Types */
public enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN,
    COMMA, DOT,

    // One or two character tokens.
    BANG_EQUAL,
    COLON_MINUS,
    EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals.
    IDENTIFIER, STRING, NUMBER, KEYWORD,

    // Reserved Words.
    NOT, WHERE,

    EOF
}
