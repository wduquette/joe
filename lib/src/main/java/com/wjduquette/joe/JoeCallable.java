package com.wjduquette.joe;

/**
 * An object that can be called as a function in a Joe script.
 */
public interface JoeCallable {
    /**
     * Calls the callable, returning the result.
     * @param joe The Joe interpreter
     * @param args The arguments to the callable
     * @return The callable's result
     * @throws JoeError on any runtime error.
     */
    Object call(Joe joe, ArgQueue args);
}
