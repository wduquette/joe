package com.wjduquette.joe;

import java.io.PrintStream;
import java.util.List;
import com.wjduquette.joe.SourceBuffer.Span;

/**
 * An error found while processing a Joe script prior to
 * execution.
 */
public class SyntaxError extends RuntimeException {
    /** The individual error messages, by line. */
    private final List<Detail> details;

    /**
     * A specific syntax error
     * @param span The span of text in the input.
     * @param message The error message.
     */
    public record Detail(Span span, String message) {
        public Detail {
            if (span.isSynthetic()) {
                throw new IllegalArgumentException(
                    "SyntaxError.Detail created with synthetic span: " + span);
            }
        }

        public int line() {
            return span.startLine();
        }

        @Override
        public String toString() {
            return "[line " + span.startLine() + "] " + message;
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
