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

        public String verbose() {
            return simple() + "\n" +
                "  In script '" + span.filename() + "':\n" +
                errorLines().stripTrailing();
        }

        private String errorLines() {
            var line = span.startLine();
            var start = Math.max(line - 1, 1);
            var end = Math.min(line + 1, span.buffer().lineCount());

            var buff = new StringBuilder();
            for (int i = start; i <= end; i++) {
                buff.append(String.format("    %03d %s\n",
                    i, span.buffer().line(i)));
            }
            return buff.toString();
        }

        public String simple() {
            return toString();
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
    public List<Detail> getDetails() {
        return details;
    }

    /**
     * Prints the list of errors by line number.
     * @param out The output stream.
     */
    public void printDetails(PrintStream out) {
        if (!details.isEmpty()) {
            out.println(details.removeFirst().verbose());
            details.forEach(d -> out.println(d.simple()));
        }
    }

    /**
     * Prints the list of errors by line number to System.out.
     */
    public void printDetails() {
        printDetails(System.out);
    }
}
