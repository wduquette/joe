package com.wjduquette.joe;

import com.wjduquette.joe.SourceBuffer.Span;

/**
 * An unexpected Java runtime error found during script execution by Joe's core
 * interpreter.
 */
public class UnexpectedError extends JoeError {
    /**
     * Creates an UnexpectedError.  These errors are thrown by
     * the Engine, and are initialized with the source context where
     * the error occurred.
     *
     * @param context The context
     * @param message The error message
     */
    public UnexpectedError(Span context, String message) {
        super(message);
        setPendingContext(context);
    }
}
