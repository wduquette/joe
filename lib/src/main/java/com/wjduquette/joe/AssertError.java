package com.wjduquette.joe;

/**
 * The error thrown by the `assert` statement
 */
public class AssertError extends JoeError {
    public AssertError(String message) {
        super(message);
    }

    public AssertError(int line, String message) {
        super(line, message);
    }
}