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
     * Creates a list of the given size, with each element
     * initialized to the given value.
     * @param size The list size
     * @param initValue The initial value to assign to each element.
     */
    public ListValue(int size, Object initValue) {
        super(size);
        for (var i = 0; i < size; i++) {
            add(initValue);
        }
    }
}
