package com.wjduquette.joe;

public class RuntimeError extends JoeError {
    final int line;
    final String lexeme;

    RuntimeError(Token token, String message) {
        super(message);
        this.line = token.line();
        this.lexeme = token.lexeme();
    }

    int line() {
        return line;
    }
}
