package com.wjduquette.joe;

enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    BACK_SLASH, COLON, COMMA, DOT, MINUS, PLUS, QUESTION,
    SEMICOLON, SLASH, STAR,

    // One or two character tokens.
    AND,
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    MINUS_GREATER,
    LESS, LESS_EQUAL,
    OR,

    // Literals.
    IDENTIFIER, STRING, NUMBER, KEYWORD,

    // Reserved Words.
    ASSERT, BREAK, CLASS, CONTINUE, ELSE, EXTENDS, FALSE, FOR, FOREACH,
    FUNCTION, IF, METHOD, NULL, RETURN, STATIC, SUPER, THIS, THROW, TRUE, VAR,
    WHILE,

    EOF
}
