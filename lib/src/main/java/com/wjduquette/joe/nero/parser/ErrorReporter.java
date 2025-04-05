package com.wjduquette.joe.nero.parser;

import com.wjduquette.joe.Trace;

public interface ErrorReporter {
    void reportError(Trace trace);
}
