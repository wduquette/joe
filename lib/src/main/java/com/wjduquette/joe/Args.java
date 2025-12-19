package com.wjduquette.joe;

import com.wjduquette.joe.types.ListValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An argument queue, as passed to Joe callables.  The Args presents
 * the arguments both as a list for indexed access and as a queue for
 * polling using the {@code next()} method.
 */
public final class Args {
    //-------------------------------------------------------------------------
    // Static constants and methods

    /**
     * An empty argument queue.
     */
    public static final Args EMPTY = new Args();

    /**
     * Creates an `Args` given zero or more argument values.
     * @param args The values
     * @return The `Args
     */
    public static Args of(Object... args) {
        return new Args(args);
    }

    /**
     * Returns a "Wrong number of arguments" JoeError for a method or function
     * with the given signature.  This is primarily used by the arity checker
     * methods, but can also be used by native functions and methods at need.
     * @param signature The signature
     * @return The error to be thrown.
     */
    public static JoeError arityFailure(String signature) {
        return new JoeError(arityFailureMessage(signature));
    }

    /**
     * Returns a "Wrong number of arguments" message string for a method or
     * function with the given signature.  This is primarily used by the
     * current `Engine` when generating runtime errors.
     * @param signature The signature
     * @return The error message.
     */
    public static String arityFailureMessage(String signature) {
        return "Wrong number of arguments, expected: " + signature + ".";
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
    // Arity Checks

    /**
     * Throws an arity check failure if the {@code size()} of the arguments
     * array differs from the expected number of arguments.
     * @param arity The expected arity
     * @param signature The signature string.
     * @throws JoeError on failure
     */
    public void exactArity(int arity, String signature) {
        if (size() != arity) {
            throw arityFailure(signature);
        }
    }

    /**
     * Throws an arity check failure if the {@code size()} of the arguments
     * array is less than the expected minimum number of arguments.
     * @param minArity The minimum arity
     * @param signature The signature string.
     * @throws JoeError on failure
     */
    public void minArity(int minArity, String signature) {
        if (size() < minArity) {
            throw arityFailure(signature);
        }
    }

    /**
     * Throws an arity check failure if the {@code size()} of the arguments
     * falls outside the expected range.
     * @param minArity The minimum arity
     * @param maxArity The maximum arity
     * @param signature The signature string.
     * @throws JoeError on failure
     */
    public void arityRange(
        int minArity,
        int maxArity,
        String signature)
    {
        if (size() < minArity || size() > maxArity) {
            throw arityFailure(signature);
        }
    }

    //-------------------------------------------------------------------------
    // Args Array API
    //
    // This portion of the API deals with the provided arguments as a
    // constant array.

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

    //-------------------------------------------------------------------------
    // Argument Queue API
    //
    // This portion of the API deals with arguments array as a queue.

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
    public boolean hasNext() {
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
    public Object next(int index) {
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
     * If there is a single argument remaining, and it's a collection,
     * returns the collection's content as a new Args value. Otherwise,
     * returns the argument list unchanged.
     * This method allows a Joe method's argument list to end with
     * a collection of values passed as a single list or as multiple arguments.
     * @return The args
     */
    public Args expandOrRemaining() {
        if (remaining() == 1 && next(0) instanceof Collection<?> c) {
            ++next;
            return new Args(c.toArray());
        } else {
            return this;
        }
    }

    //-------------------------------------------------------------------------
    // Object API

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
