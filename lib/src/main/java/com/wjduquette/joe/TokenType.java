package com.wjduquette.joe;

enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    // One or two character tokens.
    AND,
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    OR,

    // Literals.
    IDENTIFIER, STRING, NUMBER,

    // Keywords.
    CLASS, ELSE, FALSE, FUN, FOR, IF, NULL,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    EOF
}
