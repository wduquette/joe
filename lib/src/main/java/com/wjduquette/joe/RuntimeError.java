package com.wjduquette.joe;

/**
 * A runtime error found during script execution by Joe's core
 * interpreter.
 */
public class RuntimeError extends JoeError {
    public RuntimeError(int line, String message, String... frames) {
        super(line, message, frames);
    }
}
