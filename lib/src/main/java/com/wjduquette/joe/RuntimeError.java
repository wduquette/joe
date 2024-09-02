package com.wjduquette.joe;

public class RuntimeError extends JoeError {
    RuntimeError(Token token, String message, String... frames) {
        super(token.line(), message, frames);
    }
}
