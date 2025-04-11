package com.wjduquette.joe.parser;

import com.wjduquette.joe.Trace;

/**
 * The interface used to report errors encountered while parsing.  The
 * traces are usually accumulated and added to a SyntaxError exception to
 * report the error to Joe's client.
 */
public interface ErrorReporter {
    /**
     * Reports a scanning/parsing error.  The "incomplete" flag is set if
     * the error occurred at the very end of input, e.g., due to an unterminated
     * string, block, etc.
     * @param trace The source span and error message
     * @param incomplete true or false
     */
    void reportError(Trace trace, boolean incomplete);
}
