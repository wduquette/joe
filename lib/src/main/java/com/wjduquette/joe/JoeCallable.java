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
    Object call(Joe joe, Args args);

    /**
     * Gets the type of the callable, e.g., "function", for use in
     * error messages, stack traces, etc.
     * @return The type string
     */
    String callableType();

    /**
     * Gets the signature of the callable, e.g., "myName(a, b, c)", for use in
     * error messages, stack traces, etc.
     * @return The signature string
     */
    String signature();

    /**
     * Returns true if the callable is scripted, and false otherwise.
     * @return true or false;
     */
    boolean isScripted();
}
