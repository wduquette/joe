package com.wjduquette.joe.nero;

/** Nero Token Types */
enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN,
    COMMA, DOT,

    // One or two character tokens.
    COLON_MINUS,

    // Literals.
    IDENTIFIER, STRING, NUMBER, KEYWORD,

    // Reserved Words.
    NOT,

    EOF
}
