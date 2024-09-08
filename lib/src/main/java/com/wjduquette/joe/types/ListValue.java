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
     */
    public ListValue(Collection<?> values) {
        addAll(values);
    }
}
