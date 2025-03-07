package com.wjduquette.joe;

import java.util.Collection;

/**
 * An interface for a Java type implemented to play well with Joe.  A
 * Joe object knows the name of its type, can have mutable or immutable
 * fields, can optionally be iterated over, and has a string representation.
 */
public interface JoeObject extends HasTypeName {
    /**
     * Gets the object's type name.
     * @return The name
     */
    String typeName();

    /**
     * Get the value of the named object property.
     * @param name The name
     * @return The value
     * @throws JoeError if the property does not exist
     */
    Object get(String name);

    /**
     * Sets the value of the named object property.
     * @param name The name
     * @param value The value
     * @throws JoeError if the object does not allow the property to
     * be set.
     */
    void set(String name, Object value);

    /**
     * Returns true if the object has the given field, and false otherwise.
     * Note: this checks for actual fields, not method properties.
     * @param name The field name
     * @return true or false
     */
    boolean hasField(String name);

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
}
