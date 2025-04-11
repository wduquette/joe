package com.wjduquette.joe;

import com.wjduquette.joe.scanner.SourceBuffer.Span;

/**
 * An unexpected Java exception error caught during script execution by
 * Joe's engine.
 */
public class UnexpectedError extends JoeError {
    /**
     * Creates an UnexpectedError.  These errors are thrown by
     * the Engine, and are initialized with the source context where
     * the error occurred.
     *
     * @param context The context
     * @param message The error message
     * @param cause The unexpected exception.
     */
    public UnexpectedError(Span context, String message, Throwable cause) {
        super(message, cause);
        if (context != null) {
            setPendingContext(context);
        }
    }
}
