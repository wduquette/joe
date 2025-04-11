package com.wjduquette.joe.parser;

import com.wjduquette.joe.Trace;

public interface ErrorReporter {
    void reportError(Trace trace, boolean incomplete);
}
