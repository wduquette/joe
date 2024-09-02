package com.wjduquette.joe;

import java.util.List;

/**
 * An error found while processing a Joe script prior to
 * execution.
 */
public class SyntaxError extends JoeError {
    public record Detail(int line, String message) {
        @Override
        public String toString() {
            return "[" + line + "] " + message;
        }
    }
    private final List<Detail> details;

    SyntaxError(String message, List<Detail> details) {
        super(message);
        this.details = details;
    }

    @SuppressWarnings("unused")
    public List<Detail> getErrorsByLine() {
        return details;
    }

    public void printErrorsByLine() {
         details.forEach(d -> System.out.println(d.toString()));
    }
}
