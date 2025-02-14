package com.wjduquette.joe.bert;

import com.wjduquette.joe.Trace;

interface ErrorReporter {
    void reportError(Trace trace, boolean incomplete);
}
