package com.wjduquette.joe.types;

import com.wjduquette.joe.JoeList;

import java.util.ArrayList;
import java.util.Collection;

/**
 * JoeList is the standard List type in Joe.  ListValue is the standard
 * implementation used for most mutable lists.
 */
public class ListValue
    extends ArrayList<Object>
    implements JoeList
{
    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an empty list.
     */
    @SuppressWarnings("unused")
    public ListValue() {
        // nothing to do
    }

    /**
     * Creates a list containing the desired elements.
     * @param values The list's initial values.
     */
    public ListValue(Collection<?> values) {
        addAll(values);
    }

    /**
     * Creates a list with the given initial capacity.
     * @param capacity The capacity
     */
    public ListValue(int capacity, Object initValue) {
        super(capacity);
        for (var i = 0; i < capacity; i++) {
            add(initValue);
        }
    }
}
