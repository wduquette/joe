package com.wjduquette.joe;

/**
 * A "break" in a Joe loop.
 */
class Break extends RuntimeException {
    Break() {
        super(null, null, false, false);
    }
}
