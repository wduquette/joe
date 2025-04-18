package com.wjduquette.joe;

import com.wjduquette.joe.scanner.SourceBuffer.Span;

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
    /**
     * Gets whether the trace is related to a specific source location.
     * @return true or false
     */
    public boolean hasContext() {
        return context != null;
    }

    /**
     * Gets the source location's line number.
     * @return The line number.
     */
    public int line() {
        return context.startLine();
    }
}
