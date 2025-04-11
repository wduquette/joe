package com.wjduquette.joe.walker;

import com.wjduquette.joe.scanner.SourceBuffer.Span;

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

    @Override
    public String toString() {
        return type + "[" + lexeme() + "," + literal + "," + span.startLine() + "]";
    }
}
