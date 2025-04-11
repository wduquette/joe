package com.wjduquette.joe.bert;

import com.wjduquette.joe.Trace;

public interface ErrorReporter {
    void reportError(Trace trace, boolean incomplete);
}
