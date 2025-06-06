package com.wjduquette.joe;

import com.wjduquette.joe.nero.Fact;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * An interface for a Java value type implemented to play well with Joe.  A
 * JoeValue knows the name of its type, can have mutable or immutable
 * fields, can optionally be iterated over, and has a string representation.
 * In addition, every JoeValue is potentially a
 * {@link com.wjduquette.joe.nero.Fact}, provided that it has visible
 * fields.
 */
public interface JoeValue extends Fact {
    /**
     * Gets the object's type.
     * @return The type.
     */
    JoeType type();

    /**
     * Returns true if the object has the named field property, and
     * false otherwise. Note: this checks only for actual fields, not
     * for method properties.
     * @param name The field name
     * @return true or false
     */
    boolean hasField(String name);

    /**
     * Gets an unmodifiable list of the names of the value's known fields.
     * @return The list
     */
    List<String> getFieldNames();

    /**
     * Checks whether or not the value has named fields.
     * @return true or false
     */
    default boolean hasFields() {
        return !getFieldNames().isEmpty();
    }

    /**
     * Get the value of the named object property, a method or a field.
     * @param name The name
     * @return The value
     * @throws JoeError if the property does not exist
     */
    Object get(String name);

    /**
     * Sets the value of the named field property.
     * @param name The name
     * @param value The value
     * @throws JoeError if the object does not allow the property to
     * be set.
     */
    void set(String name, Object value);

    /**
     * Whether the object supports iteration with foreach or not.
     * @return true or false
     */
    default boolean canIterate() { return false; }

    /**
     * If canIterate(), returns the collection to iterate over.
     * @return The collection
     * @throws UnsupportedOperationException if !canIterate()
     */
    default Collection<?> getItems() {
        throw new UnsupportedOperationException(
            "Type does not support iteration.");
    }

    /**
     * Returns the Joe-specific string representation.
     * @param joe The interpreter
     * @return The string.
     */
    default String stringify(Joe joe) {
        return toString();
    }

    //-------------------------------------------------------------------------
    // Fact API

    /**
     * As a fact, a JoeValue's "relation" is its type name.
     * @return The relation
     */
    @Override
    default String relation() {
        return type().name();
    }

    /**
     * By default, JoeValues do not have ordered fields.  Values that
     * do have ordered fields should override both this and getFields().
     * @return true or false
     */
    @Override default boolean hasOrderedFields() { return false; }

    /**
     * If the value's type hasOrderedFields(), the list of field
     * values.  Values that do have ordered fields should override
     * this.
     * @return the list
     * @throws IllegalStateException if !isIndexed.
     */
    default List<Object> getFields() {
        throw new IllegalStateException(
            "Fact does not have ordered fields!");
    }

    /**
     * Gets a map of the fact's field values by name.
     * Values that do have fields should override this.
     * @return The map
     */
    default Map<String, Object> getFieldMap() {
        throw new IllegalStateException(
            "Fact does not have fields fields!");
    }
}
