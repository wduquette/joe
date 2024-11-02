package com.wjduquette.joe.walker;

import com.wjduquette.joe.SourceBuffer;

import static com.wjduquette.joe.SourceBuffer.Span;

record Token(
    TokenType type,
    Span span,
    Object literal
) {
    public static Token synthetic(String lexeme) {
        return new Token(null, SourceBuffer.synthetic(lexeme), null);
    }

    /**
     * Returns the token's lexeme.
     * @return The lexeme
     */
    public String lexeme() {
        return span.text();
    }

    public int line() {
        return span.startLine();
    }

    @Override
    public String toString() {
        return type + "[" + lexeme() + "," + literal + "," + span.startLine() + "]";
    }
}
