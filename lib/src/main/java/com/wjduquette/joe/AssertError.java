package com.wjduquette.joe;

import com.wjduquette.joe.SourceBuffer.Span;

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
     * Creates an assert error with the given source context.
     * @param context The location of the error in the source code.
     * @param message The message
     */
    public AssertError(Span context, String message) {
        super(message);
        pendingContext(context);
    }
}
