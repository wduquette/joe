package com.wjduquette.joe.walker;

/**
 * A "break" in a Joe loop.
 */
class Break extends RuntimeException {
    Break() {
        super(null, null, false, false);
    }
}
