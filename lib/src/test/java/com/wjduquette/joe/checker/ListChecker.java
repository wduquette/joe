package com.wjduquette.joe.checker;

import java.util.List;
import java.util.Objects;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ListChecker<Value> {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final List<Value> list;

    //-------------------------------------------------------------------------
    // Constructor

    ListChecker(List<Value> list) {
        this.list = list;
    }

    //-------------------------------------------------------------------------
    // Checkers

    public ListChecker<Value> eq(List<Value> expected) {
        if (Objects.equals(list, expected)) {
            return this;
        } else {
            throw new AssertionError("eq: expected '" + expected +
                "\", got: '" + list + "\"");
        }
    }

    public ListChecker<Value> ne(Value unexpected) {
        if (!Objects.equals(list, unexpected)) {
            return this;
        } else {
            throw new AssertionError("ne: did not expect '" + unexpected +
                "', got: '" + list + "'");
        }
    }

    @SafeVarargs
    public final ListChecker<Value> items(Value... values) {
        var items = List.of(values);

        if (list.size() != items.size()) {
            throw new AssertionError(
                "Size mismatch: expected " + items.size() +
                    " item(s), got: " + list.size() + " item(s).");
        }

        for (var i = 0; i < items.size(); i++) {
            if (!Objects.equals(list.get(i), items.get(i))) {
                throw new AssertionError(
                    "Item [" + i + "] mismatch: expected '" +
                        items.get(i) + "', got: '" +
                        list.get(i) + "'.");
            }
        }

        return this;
    }
}
