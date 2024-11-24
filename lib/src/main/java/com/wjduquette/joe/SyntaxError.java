package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.List;

/**
 * An error found while processing a Joe script prior to
 * execution.
 */
public class SyntaxError extends JoeError {
    //-------------------------------------------------------------------------
    // Constructor

    public SyntaxError(String message, List<Trace> traces) {
        super(message);
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
            .map(t -> "[line " + t.line() + "] " + t.message())
            .forEach(list::add);

        return String.join("\n", list);
    }

    private String verbose(Trace trace) {
        return "[line " + trace.line() + "] " + trace.message() + "\n" +
            "  In script '" + trace.context().filename() + "':\n" +
            errorLines(trace.context(), "    ");
    }

    public String getErrorReport() {
        return getTraceReport();
    }
}
