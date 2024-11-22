package com.wjduquette.joe;

import com.wjduquette.joe.SourceBuffer.Span;

/**
 * A runtime error found during script execution by Joe's core
 * interpreter.
 */
public class RuntimeError extends JoeError {
    /**
     * Creates a RuntimeError.  RuntimeErrors are thrown by
     * the Engine, and are initialized with the source context where
     * the error occurred.
     *
     * @param context The context
     * @param message The error message
     */
    public RuntimeError(Span context, String message) {
        super(message);
        pendingContext(context);
    }
}
