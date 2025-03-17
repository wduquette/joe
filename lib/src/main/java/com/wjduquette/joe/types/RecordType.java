package com.wjduquette.joe.types;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.ProxyType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * RecordType is the ProxyType for Joe record values implemented in Java;
 * see CatchResultType and CatchResult for examples.
 * @param <R> The value type
 */
public class RecordType<R> extends ProxyType<R> {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The record's field names, in order.
    private final List<String> fieldNames = new ArrayList<>();
    private final Map<String, Function<R,Object>> getters = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Initializes the record type
     * @param name The type's name
     */
    public RecordType(String name) {
        super(name);
    }

    //-------------------------------------------------------------------------
    // JoeType API

    @Override
    public boolean isRecordType() {
        return true;
    }

    //-------------------------------------------------------------------------
    // Builders

    /**
     * Adds a field and its getter to the type.
     * @param fieldName The field's name
     * @param getter The getter
     */
    protected void recordField(String fieldName, Function<R, Object> getter) {
        fieldNames.add(fieldName);
        getters.put(fieldName, getter);
    }

    //-------------------------------------------------------------------------
    // ProxyType Policy Methods

    /**
     * Returns a stringified value, i.e., a value for display.
     * @param joe The engine
     * @param value A value of the proxied type
     * @return The string
     */
    public String stringify(Joe joe, Object value) {
        var fieldValues = fieldNames.stream()
            .map(n -> joe.stringify(get(this, n)))
            .collect(Collectors.joining(", "));
        return name() + "(" + fieldValues + ")";
    }

    /**
     * Returns true if the value has a field with the given name, and
     * false otherwise.
     * @param value A value of the proxied type
     * @param fieldName The field name
     * @return true or false
     */
    @SuppressWarnings("unused")
    public boolean hasField(Object value, String fieldName) {
        return getters.containsKey(fieldName);
    }

    /**
     * Returns a list of the names of the value's fields.
     * @param value A value of the proxied type
     * @return The list
     */
    @SuppressWarnings("unused")
    public List<String> getFieldNames(Object value) {
        return fieldNames;
    }

    /**
     * Gets the value of the named property.  Throws an
     * "Undefined property" error if there is no such property.
     * @param value A value of the proxied type
     * @param propertyName The property name
     * @return The property value
     */
    @SuppressWarnings("unchecked")
    public Object get(Object value, String propertyName) {
        var method = bind(value, propertyName);

        if (method != null) {
            return method;
        }

        var getter = getters.get(propertyName);
        if (getter != null) {
            return getter.apply((R)value);
        }

        throw new JoeError("Undefined property '" +
            propertyName + "'.");
    }

    /**
     * Sets the value of the named field.
     *
     * <p>Proxied types are assumed not to have fields, so this always
     * throws a JoeError. Subclasses may override.</p>
     * @param value A value of the proxied type
     * @param fieldName The field name
     * @param other The value to
     * @return The property value
     */
    @SuppressWarnings("unused")
    public Object set(Object value, String fieldName, Object other) {
        throw new JoeError("Values of type " + name() +
            " have no mutable properties.");
    }
}
