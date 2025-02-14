package com.wjduquette.joe;

import com.wjduquette.joe.SourceBuffer.Span;

/**
 * A message associated with a JoeError, possibly with a script location.
 * For SyntaxError, traces are used to track individual syntax errors; for
 * execution errors, traces are used to add stack trace or other information
 * to the error.
 * @param context The location in the source code.
 * @param message The error message
 */
public record Trace(
    Span context,
    String message
) {
    public boolean hasContext() {
        return context != null;
    }

    public int line() {
        return context.startLine();
    }
}
