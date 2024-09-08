package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ArgQueue {
    public static final ArgQueue EMPTY = new ArgQueue();
    //-------------------------------------------------------------------------
    // Instance variables

    private final List<Object> args;
    private int next = 0;

    //-------------------------------------------------------------------------
    // Constructor

    public ArgQueue() {
        this(new ArrayList<>());
    }

    public ArgQueue(List<?> args) {
        this.args = Collections.unmodifiableList(args);
    }

    //-------------------------------------------------------------------------
    // Public API

    /**
     * Returns the number of remaining arguments.
     * @return the number.
     */
    public int size() {
        return args.size() - next;
    }

    /**
     * Gets whether the queue is empty or not.
     * @return true or false
     */
    public boolean isEmpty() {
        return next >= args.size();
    }

    /**
     * Returns the next argument.
     * @return The argument
     * @throws IllegalStateException if the queue is empty.
     */
    public Object next() {
        if (next >= args.size()) {
            throw new IllegalStateException(
                "next() called when ArqQueue is empty.");
        }
        return args.get(next++);
    }

    /**
     * Gets an argument by index from the remaining arguments.
     * @param index The index
     * @return the argument
     */
    public Object get(int index) {
        if (index < 0 || index >= size()) {
            throw new IllegalArgumentException(
                "Expected index in range 0 < index < " + size() +
                ", got: " + index);
        }
        return args.get(next + index);
    }

    /**
     * Returns the remainder of the arguments as an unmodifiable
     * list.
     * @return The list
     */
    public List<Object> remainder() {
        return args.subList(next, args.size());
    }

    /**
     * Returns the original, unmodifiable argument list.
     * @return The list
     */
    public List<Object> asList() {
        return args;
    }
}
