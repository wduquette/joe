package com.wjduquette.joe;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.wjduquette.joe.scanner.SourceBuffer;
import com.wjduquette.joe.scanner.SourceBuffer.Span;

/**
 * This is the base class for all error exceptions thrown by Joe at
 * runtime.  It can include line number information, as well as a
 * "stack trace" built up by the Joe interpreter.
 */
public class JoeError extends RuntimeException {
    //-------------------------------------------------------------------------
    // Instance Variables

    /** The stack trace and information messages included in the error. */
    private final List<Trace> traces = new ArrayList<>();

    /**
     * The context of the error.  This is initially set by `RuntimeError`,
     * and is reset as the error is passed up the call chain so that it
     * has the correct value as the caller adds it to the stack trace.
     */
    private SourceBuffer.Span pendingContext = null;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a Joe compilation/execution error.
     * @param message The error message.
     */
    public JoeError(String message) {
        super(message);
    }

    /**
     * Creates a Joe compilation/execution error with the given cause.
     * @param message The error message.
     * @param cause The causing exception.
     */
    public JoeError(String message, Throwable cause) {
        super(message, cause);
    }

    //-------------------------------------------------------------------------
    // Pending Context

    /**
     * Sets the error's context for use by the next catcher of this exception.
     * For internal use only.
     * @param pendingContext The context
     */
    void setPendingContext(Span pendingContext) {
        this.pendingContext = pendingContext;
    }

    //-------------------------------------------------------------------------
    // Trace Information
    //
    // Traces can be stack frames or informational.  Stack frames proper
    // will have source context; informational traces will not, and will
    // usually add information to the following stack frame.

    /**
     * Adds a call frame trace to the error, using the new context as
     * the error context and saving the new context as the pending
     * context.
     * @param newContext A span indicating the new local context, or null for
     *                   native code.
     * @param message The trace message
     * @return this
     */
    @SuppressWarnings("UnusedReturnValue")
    public final JoeError addFrame(Span newContext, String message) {
        traces.add(new Trace(newContext, message));
        pendingContext = newContext;
        return this;
    }

    /**
     * Adds a call frame trace to the error, using the pending context as
     * the error context and saving the new context as the pending
     * context.
     * @param newContext A span indicating the new local context, or null for
     *                   native code.
     * @param message The trace message
     * @return this
     */
    public final JoeError addPendingFrame(Span newContext, String message) {
        traces.add(new Trace(pendingContext, message));
        pendingContext = newContext;
        return this;
    }

    /**
     * Adds a call frame trace to the error, retaining the current
     * context.
     * @param message The trace message
     * @return this
     */
    public final JoeError addFrame(String message) {
        traces.add(new Trace(pendingContext, message));
        return this;
    }

    /**
     * Adds an informational trace to the error.
     * @param message The message
     * @return this
     */
    public final JoeError addInfo(String message) {
        traces.add(new Trace(null, message));
        return this;
    }

    /**
     * Adds an informational trace to the error, with a context to be
     * used by the subsequent frame.
     * @param context The context
     * @param message The message
     * @return this
     */
    public final JoeError addInfo(Span context, String message) {
        traces.add(new Trace(null, message));
        pendingContext = context;
        return this;
    }

    /**
     * Returns the list of accumulated trace records.
     * Traces can be stack frames or informational.  Stack frames proper
     * will have source context; informational traces will not, and will
     * usually add information to the following stack frame.
     * @return The list
     */
    public final List<Trace> getTraces() {
        return traces;
    }

    /**
     * Produces a string corresponding to the list of traces.  By default,
     * the list is suitable for inclusion in a stack trace to be displayed
     * to the user.  Subclasses can override.
     * @return The string
     */
    public String getTraceReport() {
        var list = new ArrayList<String>();
        var includedSourceContext = false;
        for (var trace : traces) {
            if (trace.hasContext()) {
                list.add(trace.message() + " " + location(trace.context()));
                if (!includedSourceContext) {
                    includedSourceContext = true;
                    list.add(errorLines(trace.context(), "  "));
                }
            } else {
                list.add(trace.message());
            }
        }

        return String.join("\n", list);
    }

    /**
     * Returns a block of source code, with line numbers, around
     * the source location at which a runtime error occurred.
     * @param span The source location
     * @param leader An indent for each line.
     * @return The string.
     */
    protected String errorLines(Span span, String leader) {
        var line = span.startLine();
        var start = Math.max(line - 1, 1);
        var end = Math.min(line + 1, span.buffer().lineCount());

        var list = new ArrayList<String>();
        for (int i = start; i <= end; i++) {
            list.add(String.format("%s%03d %s",
                leader, i, span.buffer().line(i)));
        }
        return String.join("\n", list);
    }

    private String location(Span context) {
        return "(" +
            context.buffer().filename() + ":" +
            context.startLine() + ")";
    }


    /**
     * Gets the complete stack trace string.
     * @return The stack trace
     */
    public String getJoeStackTrace() {
        if (traces.isEmpty()) {
            return getMessage();
        } else {
            return getMessage() + "\n" + getTraceReport().indent(2);
        }
    }

    /**
     * Gets the Java stack trace for this exception.
     * @return The trace.
     */
    public String getJavaStackTrace() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream recordingStream = new PrintStream(baos);
        printStackTrace(recordingStream);
        return baos.toString();
    }

    //-------------------------------------------------------------------------
    // Code to be removed.

    /**
     * Gets the line number, or -1 if no line number information is
     * available
     * @return The line number
     */
    public int line() {
        return pendingContext != null ? pendingContext.startLine() : -1;
    }
}
