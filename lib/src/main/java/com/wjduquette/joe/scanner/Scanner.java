package com.wjduquette.joe.scanner;

import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.SourceBuffer.Span;

import static com.wjduquette.joe.scanner.TokenType.ERROR;

public class Scanner {
    public interface ErrorHandler {
        void handle(Span span, String message);
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    // The source being scanned
    private final SourceBuffer buffer;

    // The client's error handler.
    private final ErrorHandler handler;

    // The underlying tokenizer
    private Tokenizer tokenizer;

    // The sliding window of tokens
    private Token previous = null;
    private Token current = null;

    //-------------------------------------------------------------------------
    // Constructor

    public Scanner(SourceBuffer buffer, ErrorHandler handler) {
        this.buffer = buffer;
        this.handler = handler;
        this.tokenizer = new Tokenizer(buffer);

        // Prime it with the first token.
        advance();
    }

    //-------------------------------------------------------------------------
    // Public API

    public Token previous() {
        return previous;
    }

    public Token peek() {
        return current;
    }

    public void advance() {
        // FIRST, slide the window.
        previous = current;

        // NEXT, get the next non-error token.
        for (;;) {
            current = tokenizer.scanToken();
            if (current.type() == ERROR) {
                // Report the error.
                handler.handle(current.span(), (String)current.literal());
            } else {
                break;
            }
        }
    }
}
