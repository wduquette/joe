package com.wjduquette.joe;

import com.wjduquette.joe.SourceBuffer.Span;

/**
 * A message associated with a JoeError, usually to add stack trace or
 * other information to the error.
 * @param context The location in the source code.
 * @param message The error message
 */
public record Trace(
    Span context,
    String message
) {
    public static Trace frame(Span context, String message) {
        return new Trace(context, message);
    }

    public static Trace info(String message) {
        return new Trace(null, message);
    }

    public boolean hasContext() {
        return context != null;
    }

    public int line() {
        return context.startLine();
    }
}
