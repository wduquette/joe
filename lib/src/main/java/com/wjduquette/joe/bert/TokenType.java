package com.wjduquette.joe.bert;

enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    AT, BACK_SLASH, COLON, COMMA, DOT, QUESTION,
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
    FALSE, FOR, FOREACH, FUNCTION, IF, IN, METHOD, NI, NULL, RETURN,
    STATIC, SUPER, SWITCH, THIS, THROW, TRUE, VAR, WHILE,

    // Temporary Reserved Word
    PRINT,

    ERROR,
    EOF
}