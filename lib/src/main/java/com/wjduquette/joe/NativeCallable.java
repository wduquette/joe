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
     * Returns true if the callable is scripted, and false if it is native.
     * @return true or false;
     */
    boolean isScripted();

    /**
     * Returns true if the callable is native, and false if it is scripted.
     */
    default boolean isNative() { return !isScripted(); }
}
