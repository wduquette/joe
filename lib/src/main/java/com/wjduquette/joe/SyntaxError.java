package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.List;

/**
 * An error found while processing a Joe script prior to
 * execution.
 */
public class SyntaxError extends JoeError {
    //-------------------------------------------------------------------------
    // Instance Variables

    /**
     * Whether the script containing the error was "complete" or not.
     * See Joe::isComplete.
     */
    private final boolean complete;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Used by engines to indicate that a script could not be compiled.  The
     * message is usually fairly generic; the real information is in the
     * traces, and should be retrieved using {@code getTraceReport}.
     *
     * <p>The {@code complete} flag is false if the parser did not receive a
     * complete script: that is, compilation failed only because of an
     * incomplete construct at the end of the script, e.g., a non-terminated
     * string.</p>
     * @param message The main error message
     * @param traces The specific errors, with location
     * @param complete true or false
     */
    public SyntaxError(String message, List<Trace> traces, boolean complete) {
        super(message);
        this.complete = complete;
        getTraces().addAll(traces);
    }

    //-------------------------------------------------------------------------
    // Reporting

    @Override
    public String getJoeStackTrace() {
        return getMessage();
    }

    @Override
    public String getTraceReport() {
        if (getTraces().isEmpty()) {
            return "";
        }

        var list = new ArrayList<String>();
        list.add(verbose(getTraces().removeFirst()));
        getTraces().stream()
            .map(t -> t.hasContext()
                ? "[line " + t.line() + "] " + t.message()
                : t.message())
            .forEach(list::add);

        return String.join("\n", list);
    }

    private String verbose(Trace trace) {
        // Note: for syntax errors there should always be a context.
        // Once Bert is fully functional, this if-check can be removed.
        if (trace.hasContext()) {
            return "[line " + trace.line() + "] " + trace.message() + "\n" +
                "  In script '" + trace.context().filename() + "':\n" +
                errorLines(trace.context(), "    ");
        } else {
            return trace.message();
        }
    }

    /**
     * Gets the report of syntax errors.
     * @return The string
     */
    public String getErrorReport() {
        return getTraceReport();
    }

    /**
     * Returns whether the parsed script was complete or not.  A script is
     * incomplete if parsing failed only because of a non-terminated
     * construct, e.g., a non-terminated string. The parser expected
     * more tokens, but got to the end of the script.
     *
     * <p>To put it another way, a script is incomplete if (A) it is in
     * error and (B) adding more code to the end might fix that.</p>
     * @return true or false
     */
    public boolean isComplete() {
        return complete;
    }
}
