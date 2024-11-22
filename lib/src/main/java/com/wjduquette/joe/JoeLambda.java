package com.wjduquette.joe;

/**
 * A functional interface used when defining native JoeCallables.
 */
public interface JoeLambda {
    /**
     * Calls the callable, returning the result.
     * @param joe The Joe interpreter
     * @param args The arguments to the callable
     * @return The callable's result
     * @throws JoeError on any runtime error.
     */
    Object call(Joe joe, Args args);
}
