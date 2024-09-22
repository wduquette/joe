package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An argument queue, as passed to Joe callables.  The ArgQueue presents
 * the arguments both as a list for indexed access and as a queue for
 * polling using the {@code next()} method.
 */
public final class ArgQueue {
    /**
     * An empty argument queue.
     */
    public static final ArgQueue EMPTY = new ArgQueue();

    //-------------------------------------------------------------------------
    // Instance variables

    private final List<Object> args;
    private int next = 0;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an empty argument queue.
     */
    public ArgQueue() {
        this(new ArrayList<>());
    }

    /**
     * Creates an argument queue containing the given list of arguments.
     * @param args the arguments.
     */
    public ArgQueue(List<?> args) {
        this.args = Collections.unmodifiableList(args);
    }

    //-------------------------------------------------------------------------
    // Public API

    /**
     * The total number of arguments.
     * @return the number
     */
    public int size() {
        return args.size();
    }

    /**
     * Gets whether the queue has any arguments or not.
     * @return true or false.
     */
    public boolean isEmpty() {
        return args.isEmpty();
    }

    /**
     * Gets the argument from the complete list of arguments.
     * @param index The index
     * @return The value.
     */
    public Object get(int index) {
        return args.get(index);
    }

    /**
     * Returns the number of remaining arguments, i.e., those not yet retrieved
     * using {@code next()}.
     * @return the number.
     */
    public int remainingArgs() {
        return args.size() - next;
    }

    /**
     * Gets whether the queue has any remaining arguments or not, i.e.,
     * those not yet retrieved using {@code next()}.
     * @return true or false
     */
    public boolean hasRemaining() {
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
    public Object getRemaining(int index) {
        if (index < 0 || index >= remainingArgs()) {
            throw new IllegalArgumentException(
                "Expected index in range 0 < index < " + remainingArgs() +
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

    @Override
    public String toString() {
        var before = args.subList(0, next).stream().map(Object::toString).collect(Collectors.joining(","));
        var after = args.subList(next, args.size()).stream().map(Object::toString).collect(Collectors.joining(","));
        return "ArgQueue[" + before + "/" + after + "]";
    }
}
