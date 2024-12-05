package com.wjduquette.joe.expander;

import com.wjduquette.joe.SourceBuffer;

public record Token(TokenType type, SourceBuffer.Span span) {
    public String text() {
        return type != TokenType.EOF ? span.text() : "";
    }

    public String toString() {
        return type + "[" + text() + "]";
    }
}
