package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.List;

import com.wjduquette.joe.SourceBuffer.Span;

/**
 * This is the base class for all error exceptions thrown by Joe at
 * runtime.  It can include line number information, as well as a
 * "stack trace" built up by the Joe interpreter.
 */
public class JoeError extends RuntimeException {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The stack trace and information messages included in the error.
    private final List<Trace> traces = new ArrayList<>();

    // TO BE REPLACED! The span of source text associate with the error, or null.
    // We will replace this with the pendingContext, used to pass the
    // location of the error in a function up through the call chain.
    private final SourceBuffer.Span span;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an error with no line number info and an optional number
     * of stack frame strings.
     * @param message The error message.
     */
    public JoeError(String message) {
        this(null, message);
    }

    /**
     * Creates an error with line number info and an optional number
     * of stack frame strings.
     * @param span The source span
     * @param message The error message.
     */
    public JoeError(SourceBuffer.Span span, String message) {
        super(message);
        this.span = span;
    }

    //-------------------------------------------------------------------------
    // Trace Information
    //
    // Traces can be stack frames or informational.  Stack frames proper
    // will have source context; informational traces will not, and will
    // usually add information to the following stack frame.

    /**
     * Adds a call frame trace to the error.
     * @param context A span indicating the location of the error in the source.
     * @param message The trace message
     * @return this
     */
    public final JoeError addFrame(Span context, String message) {
        traces.add(new Trace(context, message));
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
        for (var trace : traces) {
            if (trace.hasContext()) {
                list.add(
                    trace.message() + " (" +
                    trace.context().buffer().filename() + ":" +
                    trace.context().startLine() + ")"
                );
            } else {
                list.add(trace.message());
            }
        }

        return String.join("\n", list);
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

    //-------------------------------------------------------------------------
    // Code to be removed.

    /**
     * Gets the line number, or -1 if no line number information is
     * available
     * @return The line number
     */
    public int line() {
        return span != null ? span.startLine() : -1;
    }
}
