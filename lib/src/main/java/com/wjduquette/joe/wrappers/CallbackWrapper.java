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

    public CallbackWrapper(Joe joe, Object callable) {
        this.joe = joe;
        this.callable = callable;
    }

    public Object getCallable() {
        return callable;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Object callCallable(Object... args) {
        System.out.println("Calling callable: " + joe.stringify(callable));
        return joe.call(callable, args);
    }

    public String toString() {
        return "CallbackWrapper[" + joe.stringify(callable) + "]";
    }
}
