package com.wjduquette.joe.tools;

/**
 * This exception is used to report tool-detected errors to the user.  By
 * default, such an exception terminates the tool with a human-readable
 * error message.  GUI applications are free to install their own handlers
 * in order to present these errors to the user without terminating the
 * application.
 */
@SuppressWarnings("unused")
public class ToolException extends RuntimeException {
    /**
     * Creates a ToolException.
     * @param message The message
     */
    public ToolException(String message) {
        super(message);
    }

    /**
     * Creates a ToolException with a cause
     * @param message The message
     * @param cause The cause
     */
    public ToolException(String message, Throwable cause) {
        super(message, cause);
    }
}
