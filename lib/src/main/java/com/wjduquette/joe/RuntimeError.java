package com.wjduquette.joe;

/**
 * A runtime error found during script execution by Joe's core
 * interpreter.
 */
public class RuntimeError extends JoeError {
    public RuntimeError(
        SourceBuffer.Span span,
        String message,
        String... frames
    ) {
        super(span, message, frames);
    }
}
