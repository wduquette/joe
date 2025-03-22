package com.wjduquette.joe.types;

import com.wjduquette.joe.JoeError;

/**
 * CatchResult is the type of the value returned by the standard
 * `catch(callable)` function.  On a successful call, `result` will be the
 * result returned by the callable and `error` will be null.  On failure,
 * `result` will be null and `error` will be the thrown error.
 *
 * <p>Note: `result == null` does not necessarily imply an error!
 * Always check the value of `error` to determine the OK/error status.</p>
 * @param result The successful result, or null.
 * @param error The failure error, or null
 */
public record CatchResult(Object result, JoeError error) {
    //-------------------------------------------------------------------------
    // Java API

    /**
     * Creates a CatchResult for a successful call.
     * @param result The successful result
     * @return The record
     */
    public static CatchResult ok(Object result) {
        return new CatchResult(result, null);
    }

    /**
     * Creates a CatchResult for a failed call.
     * @param error The thrown error
     * @return The record
     */
    public static CatchResult error(JoeError error) {
        return new CatchResult(null, error);
    }

    /**
     * Returns true if the call returned successfully, and false otherwise.
     * @return true or false
     */
    public boolean isOK() {
        return error == null;
    }

    /**
     * Returns true if the call threw an error, and false otherwise.
     * @return true or false
     */
    public boolean isError() {
        return error != null;
    }
}
