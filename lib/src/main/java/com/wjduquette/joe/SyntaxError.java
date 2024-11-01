package com.wjduquette.joe;

import java.io.PrintStream;
import java.util.List;

/**
 * An error found while processing a Joe script prior to
 * execution.
 */
public class SyntaxError extends RuntimeException {
    /** The individual error messages, by line. */
    private final List<Detail> details;

    /**
     * A specific syntax error
     * @param line The line number in the input.
     * @param message The error message.
     */
    public record Detail(int line, String message) {
        @Override
        public String toString() {
            return "[" + line + "] " + message;
        }
    }

    public SyntaxError(String message, List<Detail> details) {
        super(message);
        this.details = details;
    }

    /**
     * Gets the list of errors by line number.
     * @return The list
     */
    @SuppressWarnings("unused")
    public List<Detail> getErrorsByLine() {
        return details;
    }

    /**
     * Prints the list of errors by line number.
     * @param out The output stream.
     */
    public void printErrorsByLine(PrintStream out) {
        details.forEach(d -> out.println(d.toString()));
    }

    /**
     * Prints the list of errors by line number to System.out.
     */
    public void printErrorsByLine() {
        printErrorsByLine(System.out);
    }
}
