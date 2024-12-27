package com.wjduquette.joe.bert;

/**
 * This is the public face of an Upvalue, a local variable from
 * an enclosing scope whose value moves from the stack to the heap
 * when the enclosing scope leaves the call stack.
 */
public interface Upvalue {
    Object get();
    void set(Object value);
}
