package com.wjduquette.joe;

/**
 * The error thrown by Joe's `assert` statement.
 */
public class AssertError extends JoeError {
    /**
     * Creates an assert error with the given message.
     * @param message The message
     */
    public AssertError(String message) {
        super(message);
    }

    /**
     * Creates an assert error at the given line number with the
     * given message.
     * @param span The span of text related to the error.
     * @param message The message
     */
    public AssertError(SourceBuffer.Span span, String message) {
        super(span, message);
    }
}
