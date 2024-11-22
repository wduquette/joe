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

    // The context of the error.  This is initially set by `RuntimeError`,
    // and is reset as the error is passed up the call chain so that it
    // has the correct value as the caller adds it to the stack trace.
    private SourceBuffer.Span pendingContext = null;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an error with no line number info and an optional number
     * of stack frame strings.
     * @param message The error message.
     */
    public JoeError(String message) {
        super(message);
    }

    //-------------------------------------------------------------------------
    // Pending Context

    /**
     * Gets the error's context in the source code, or null if it was
     * thrown by native code.
     * @return The context
     */
    public Span getPendingContext() {
        return pendingContext;
    }

    /**
     * Sets the error's context for use by the next catcher of this exception.
     * @param pendingContext The context
     */
    public void setPendingContext(Span pendingContext) {
        this.pendingContext = pendingContext;
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
        return pendingContext != null ? pendingContext.startLine() : -1;
    }
}
