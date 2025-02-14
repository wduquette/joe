package com.wjduquette.joe.walker;

import com.wjduquette.joe.Trace;

interface ErrorReporter {
    void reportError(Trace trace, boolean incomplete);
}
