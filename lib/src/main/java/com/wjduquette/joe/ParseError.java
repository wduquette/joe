package com.wjduquette.joe;

/**
 * An error found while parsing Joe code.
 */
public class ParseError extends JoeError {
    public ParseError(String message) {
        super(message);
    }
}
