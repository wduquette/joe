package com.wjduquette.joe.scanner;

import com.wjduquette.joe.scanner.SourceBuffer.Span;

/**
 * A token produce by Bert's scanner.
 * @param type The token type
 * @param span The span in the source code
 * @param literal A literal value, e.g., for numbers, or null
 */
public record Token(
    TokenType type,
    Span span,
    Object literal
) {
    /**
     * Creates a synthetic token representing a specific lexeme.
     * @param lexeme The lexeme.
     * @return The token.
     */
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

    /**
     * Returns the starting line number for the token's span, or -1
     * for synthetic tokens.
     * @return The line number.
     */
    public int line() {
        return span != null ? span.startLine() : -1;
    }

    @Override
    public String toString() {
        return type + "[" + lexeme() + "," + literal + "," + line() + "]";
    }
}
