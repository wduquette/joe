package com.wjduquette.joe.types;

import com.wjduquette.joe.ProxyType;

import java.util.List;

/**
 * RecordType is the ProxyType for JoeRecord values implemented in Java;
 * see CatchResultType and CatchResult for examples.
 * @param <T>
 */
public class RecordType<T extends RecordValue> extends ProxyType<T> {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The record's field names, in order.
    private final List<String> fieldNames;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Initializes the record type
     * @param name The type's name
     * @param fieldNames The type's instance field names, in order.
     */
    public RecordType(String name, List<String> fieldNames) {
        super(name);
        this.fieldNames = fieldNames;
    }

    //-------------------------------------------------------------------------
    // RecordType API

    /**
     * Gets a list of the names of the type's instance field.
     * @return The list.
     */
    public List<String> getRecordFields() {
        return fieldNames;
    }
}
