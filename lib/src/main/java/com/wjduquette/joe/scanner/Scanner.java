package com.wjduquette.joe.scanner;

import com.wjduquette.joe.scanner.SourceBuffer.Span;

import static com.wjduquette.joe.scanner.TokenType.EOF;
import static com.wjduquette.joe.scanner.TokenType.ERROR;

public class Scanner {
    /**
     * The scanner's error handler interface.
     */
    public interface ErrorHandler {
        /**
         * Receives each scan error
         * @param span The span in the source code where the error occurred.
         * @param message A descriptive message.
         */
        void handle(Span span, String message);
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    // The client's error handler.
    private final ErrorHandler errorHandler;

    // The underlying tokenizer
    private final Tokenizer tokenizer;

    // The sliding window of tokens
    private Token previous = null;
    private Token current = null;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a scanner on the given source buffer.
     * On creation, previous() will return null and peek() will return the
     * next token.  The error handler might be called on creation.
     * @param buffer The source text
     * @param errorHandler The handler for scan errors.
     */
    public Scanner(SourceBuffer buffer, ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        this.tokenizer = new Tokenizer(buffer);
        advance();
    }

    //-------------------------------------------------------------------------
    // Public API

    public boolean isAtEnd() {
        return current.type() == EOF;
    }

    /**
     * Returns the previously scanned token.  This is initially null,
     * and will be non-null after the first successful advance().
     * @return The previous token.
     */
    public Token previous() {
        return previous;
    }

    /**
     * Returns the next token to process.
     * @return The token
     */
    public Token peek() {
        return current;
    }

    /**
     * Returns true if peek() has the given token type, and false otherwise.
     * @param type The type
     * @return true or false.
     */
    public boolean check(TokenType type) {
        return current.type() == type;
    }

    /**
     * Returns true if peek() has one of the given token types, and false
     * otherwise, advancing the scanner on match().  Thus, on true
     * previous() will  return the just matched token.
     * @param types The desired types
     * @return true or false.
     */
    public boolean match(TokenType... types) {
        for (var type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    /**
     * Requires that the next token have the given type.  If it does,
     * the scanner is advanced; previous() will return the consumed
     * token.  If not, an error is generated with the given message.
     * @param type The expected type
     * @param message The message
     */
    public void consume(TokenType type, String message) {
        if (match(type)) return;

        var span = current.span();
        var where = span.isAtEnd()
            ? "end"
            : "'" + span.text() + "'";

        var text = "Error at " + where + ": " + message;

        errorHandler.handle(span, text);
    }

    /**
     * Advances the scanner by one token, reporting any errors.
     */
    public void advance() {
        Token error =  null;

        // FIRST, slide the window.
        previous = current;

        // NEXT, get the next non-error token.
        for (;;) {
            current = tokenizer.scanToken();
            if (current.type() != ERROR) break;
            if (error == null) error = current;
        }

        if (error != null) {
            // Report the error.
            errorHandler.handle(error.span(), (String)error.literal());
        }
    }
}
