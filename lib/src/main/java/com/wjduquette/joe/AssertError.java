package com.wjduquette.joe;

/**
 * The error thrown by Joe's `assert` statement.
 */
public class AssertError extends JoeError {
    /**
     * Creates an assert error with the given message.
     * @param message The message
     * @param frames Explanatory "stack frames"
     */
    public AssertError(String message, String... frames) {
        super(message, frames);
    }

    /**
     * Creates an assert error at the given line number with the
     * given message.
     * @param line The line number
     * @param message The message
     */
    public AssertError(int line, String message) {
        super(line, message);
    }
}
