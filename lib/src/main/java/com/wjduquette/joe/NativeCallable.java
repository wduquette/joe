package com.wjduquette.joe;

/**
 * A Joe callable that can be called directly via its "call" method.
 */
public interface NativeCallable extends JoeCallable {
    /**
     * Calls the callable, returning the result.
     * @param joe The Joe interpreter
     * @param args The arguments to the callable
     * @return The callable's result
     * @throws JoeError on any runtime error.
     */
    Object call(Joe joe, Args args);

}
