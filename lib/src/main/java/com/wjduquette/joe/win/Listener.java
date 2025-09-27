package com.wjduquette.joe.win;

/**
 * A cancelable listener.
 */
public interface Listener {
    /**
     * Cancels the listener, removing it from the list of listeners.
     */
    void cancel();
}
