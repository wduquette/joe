package com.wjduquette.joe;

/**
 * A "continue" in a Joe loop.
 */
class Continue extends RuntimeException {
    Continue() {
        super(null, null, false, false);
    }
}
