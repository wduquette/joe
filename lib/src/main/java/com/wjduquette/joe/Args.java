package com.wjduquette.joe;

import com.wjduquette.joe.types.ListValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An argument queue, as passed to Joe callables.  The Args presents
 * the arguments both as a list for indexed access and as a queue for
 * polling using the {@code next()} method.
 */
public final class Args {
    /**
     * An empty argument queue.
     */
    public static final Args EMPTY = new Args();

    public static Args of(Object... args) {
        return new Args(args);
    }

    //-------------------------------------------------------------------------
    // Instance variables

    private final Object[] args;
    private int next = 0;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an empty argument queue.
     */
    public Args() {
        this(new Object[0]);
    }

    /**
     * Creates an argument queue given the array of arguments.
     * @param args The arguments
     */
    public Args(Object[] args) {
        this.args = args;
    }

    //-------------------------------------------------------------------------
    // Public API

    /**
     * The total number of arguments.
     * @return the number
     */
    public int size() {
        return args.length;
    }

    /**
     * Gets whether the queue has any arguments or not.
     * @return true or false.
     */
    public boolean isEmpty() {
        return args.length == 0;
    }

    /**
     * Gets the argument from the complete array of arguments.
     * @param index The index
     * @return The value.
     */
    public Object get(int index) {
        return args[index];
    }

    /**
     * Returns the number of remaining arguments, i.e., those not yet retrieved
     * using {@code next()}.
     * @return the number.
     */
    public int remaining() {
        return args.length - next;
    }

    /**
     * Gets whether the queue has any remaining arguments or not, i.e.,
     * those not yet retrieved using {@code next()}.
     * @return true or false
     */
    public boolean hasRemaining() {
        return next < args.length;
    }

    /**
     * Returns the next argument.
     * @return The argument
     * @throws IllegalStateException if the queue is empty.
     */
    public Object next() {
        if (next >= args.length) {
            throw new IllegalStateException(
                "next() called when Args queue is empty.");
        }
        return args[next++];
    }

    /**
     * Gets an argument by index from the remaining arguments.
     * @param index The index
     * @return the argument
     */
    public Object getRemaining(int index) {
        if (index < 0 || index >= remaining()) {
            throw new IllegalArgumentException(
                "Expected index in range 0 < index < " + remaining() +
                ", got: " + index);
        }
        return args[next + index];
    }

    /**
     * Returns the remainder of the arguments as an array.
     * @return The list
     */
    public Object[] remainderAsArray() {
        return Arrays.copyOfRange(args, next, args.length);
    }

    /**
     * Returns the remainder of the arguments as an unmodifiable
     * list.
     * @return The list
     */
    public List<Object> remainderAsList() {
        var list = new ListValue();
        Collections.addAll(list,
            Arrays.copyOfRange(args, next, args.length));
        return list;
    }

    /**
     * Returns the original argument array.
     * @return The array
     */
    public Object[] asArray() {
        return args;
    }

    /**
     * Returns the original arguments as a list;
     * @return The list
     */
    public ListValue asList() {
        var list = new ListValue();
        Collections.addAll(list, args);
        return list;
    }

    @Override
    public String toString() {
        var list = asList();
        var before = list.subList(0, next).stream()
            .map(Object::toString)
            .collect(Collectors.joining(","));
        var after = list.subList(next, list.size()).stream()
            .map(Object::toString)
            .collect(Collectors.joining(","));
        return "Args[" + before + "/" + after + "]";
    }
}
