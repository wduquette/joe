package com.wjduquette.joe;

/**
 * JoeCallable is a marker interface for objects that can be called as
 * functions by an Engine. It defines the methods that all callables
 * must provide to Joe client.  The client can call a JoeCallable directly
 * via the Joe::call method.</p>
 *
 * <p><b>Note:</b> A scripted callable can only be called using the engine
 * in which it was defined.  In particular, different engines implement scripted
 * functions and methods quite differently; thus, a scripted callable from one
 * engine can never be used with a different engine.</p>
 */
public interface JoeCallable {
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
