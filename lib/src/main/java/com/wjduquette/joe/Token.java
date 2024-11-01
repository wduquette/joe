package com.wjduquette.joe;

record Token(
    TokenType type,
    String lexeme,
    Object literal,
    int line
) {
    public static Token synthetic(String lexeme) {
        return new Token(null, lexeme, null, -1);
    }

    @Override
    public String toString() {
        return type + "[" + lexeme + "," + literal + "," + line + "]";
    }
}
