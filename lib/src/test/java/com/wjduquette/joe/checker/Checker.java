package com.wjduquette.joe.checker;

import java.util.List;
import java.util.Objects;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Checker<Value> {
    //-------------------------------------------------------------------------
    // Static Methods

    public static <V> Checker<V> check(V value) {
        return new Checker<>(value);
    }

    public static <V> ListChecker<V> checkList(List<V> list) {
        return new ListChecker<>(list);
    }

    public static Checker<Exception> checkThrow(Runnable runnable) {
        try {
            runnable.run();
            throw new AssertionError("checkThrows: Expected exception");
        } catch (Exception ex) {
            return check(ex);
        }
    }

    public static void fail(String message) {
        throw new AssertionError("failed: " + message);
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    private final Value value;

    //-------------------------------------------------------------------------
    // Constructor

    private Checker(Value value) {
        this.value = value;
    }

    //-------------------------------------------------------------------------
    // Checkers

    public Checker<Value> eq(Value expected) {
        if (Objects.equals(value, expected)) {
            return this;
        } else {
            throw new AssertionError("eq: expected \"" + expected +
                "\", got: \"" + value + "\"");
        }
    }

    public Checker<Value> ne(Value unexpected) {
        if (!Objects.equals(value, unexpected)) {
            return this;
        } else {
            throw new AssertionError("ne: did not expect \"" + unexpected +
                "\", got: \"" + value + "\"");
        }
    }

    public Checker<Value> hasString(String expected) {
        if (value != null && value.toString().equals(expected)) {
            return this;
        }

        throw new AssertionError("hasString: expected \"" + expected +
            "\", got: \"" + value + "\"");
    }

    public Checker<Value> containsString(String expected) {
        if (value != null && value.toString().contains(expected)) {
            return this;
        }

        throw new AssertionError("containsString: expected \"" + expected +
            "\", got: \"" + value + "\"");
    }
}
