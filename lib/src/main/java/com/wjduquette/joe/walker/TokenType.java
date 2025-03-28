package com.wjduquette.joe.walker;

enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    LEFT_BRACKET, RIGHT_BRACKET,
    AT, BACK_SLASH, COLON, COMMA, DOLLAR, DOT, MINUS, PLUS, QUESTION,
    SEMICOLON, SLASH, STAR,

    // One or two character tokens.
    AND,
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    MINUS_EQUAL,
    MINUS_GREATER,
    MINUS_MINUS,
    LESS, LESS_EQUAL,
    OR,
    PLUS_EQUAL,
    PLUS_PLUS,
    SLASH_EQUAL,
    STAR_EQUAL,

    // Literals.
    IDENTIFIER, STRING, NUMBER, KEYWORD,

    // Reserved Words.
    ASSERT, BREAK, CASE, CLASS, CONTINUE, DEFAULT, ELSE, EXTENDS,
    FALSE, FOR, FOREACH, FUNCTION, IF, IN, LET, MATCH, METHOD, NI, NULL,
    RECORD, RETURN, STATIC, SUPER, SWITCH, THIS, THROW, TRUE, VAR, WHILE,

    EOF
}
