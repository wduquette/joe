package com.wjduquette.joe.clark;

/**
 * This is the public face of an Upvalue, a local variable from
 * an enclosing scope whose value moves from the stack to the heap
 * when the enclosing scope leaves the call stack.
 */
public interface Upvalue {
    /**
     * Gets the variable's value.
     * @return The value
     */
    Object get();

    /**
     * Sets the variable's value.
     * @param value The value
     */
    void set(Object value);
}
