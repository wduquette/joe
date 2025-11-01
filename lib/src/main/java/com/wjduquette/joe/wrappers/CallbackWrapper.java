package com.wjduquette.joe.wrappers;

import com.wjduquette.joe.Joe;

/**
 * This is a wrapper for Joe callables which are passed to Java objects as
 * retained callbacks.  Callables used transiently can be passed as Java
 * lambdas; but when the callable is assigned to an object property in a
 * setter it's necessary to be able to get it back in the getter.
 * Using a wrapper like this makes it easy for a ProxyType to do the
 * wrapping and unwrapping.
 *
 * <p>This wrapper is usually used as a base class for concrete classes
 * that implement specific functional interfaces.</p>
 */
public class CallbackWrapper {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    private final Object callable;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new callback wrapper
     * @param joe The interpreter
     * @param callable The callable
     */
    public CallbackWrapper(Joe joe, Object callable) {
        this.joe = joe;
        this.callable = callable;
    }

    /**
     * Gets the callable.
     * @return the callable
     */
    public Object getCallable() {
        return callable;
    }

    /**
     * Calls the callable, passing it the arguments.
     * @param args The arguments
     * @return the callable's return value
     */
    @SuppressWarnings("UnusedReturnValue")
    public Object callCallable(Object... args) {
        return joe.call(callable, args);
    }

    @Override
    public String toString() {
        return "CallbackWrapper[" + joe.stringify(callable) + "]";
    }
}
