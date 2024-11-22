package com.wjduquette.joe;
import com.wjduquette.joe.SourceBuffer.Span;

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
     * Returns true if this is a scripted callable, and false if it is a
     * native callable.
     * @return true or false
     */
    default boolean isScripted() {
        return context() != null;
    }

    /**
     * Gets the context of the callable in the source code, or null if
     * !isScriped().
     * @return The context.
     */
    default Span context() {
        return null;
    }
}
