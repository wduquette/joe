package com.wjduquette.joe;

/**
 * A runtime error found during script execution by Joe's core
 * interpreter.
 */
public class RuntimeError extends JoeError {
    RuntimeError(Token token, String message, String... frames) {
        super(token.line(), message, frames);
    }
}
