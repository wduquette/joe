package com.wjduquette.joe;

public class JoeError extends RuntimeException {
    public JoeError(String message) {
        super(message);
    }

    /**
     * Gets the line number, or -1 if no line number information is
     * available
     * @return The line number
     */
    public int line() {
        return -1;
    }
}
