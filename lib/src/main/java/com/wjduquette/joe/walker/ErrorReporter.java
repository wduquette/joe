package com.wjduquette.joe.walker;

import com.wjduquette.joe.Trace;

public interface ErrorReporter {
    void reportError(Trace trace, boolean incomplete);
}
