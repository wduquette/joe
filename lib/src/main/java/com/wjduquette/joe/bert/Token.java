package com.wjduquette.joe.bert;

import com.wjduquette.joe.SourceBuffer.Span;

record Token(
    TokenType type,
    Span span,
    Object literal
) {
    public static Token synthetic(String lexeme) {
        return new Token(null, null, lexeme);
    }

    /**
     * Returns the token's lexeme.
     * @return The lexeme
     */
    public String lexeme() {
        return span != null ? span.text() : (String)literal;
    }

    public int line() {
        return span != null ? span.startLine() : -1;
    }

    @Override
    public String toString() {
        return type + "[" + lexeme() + "," + literal + "," + line() + "]";
    }
}
