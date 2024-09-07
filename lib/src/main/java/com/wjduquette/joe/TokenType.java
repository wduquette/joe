package com.wjduquette.joe;

enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COLON, COMMA, DOT, MINUS, PLUS, QUESTION, SEMICOLON, SLASH, STAR,

    // One or two character tokens.
    AND,
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    OR,

    // Literals.
    IDENTIFIER, STRING, NUMBER, KEYWORD,

    // Reserved Words.
    ASSERT, CLASS, ELSE, EXTENDS, FALSE, FUNCTION, FOR, IF, METHOD, NULL,
    RETURN, STATIC, SUPER, THIS, TRUE, VAR, WHILE,

    EOF
}
