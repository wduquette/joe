package com.wjduquette.joe;

import com.wjduquette.joe.nero.Fact;

import java.util.Collection;
import java.util.List;

/**
 * An interface for a Java value type implemented to play well with Joe.  A
 * JoeValue knows the name of its type, can have mutable or immutable
 * fields, can optionally be iterated over, and has a string representation.
 */
public interface JoeValue {
    /**
     * Gets the object's type.
     * @return The type.
     */
    JoeType type();

    /**
     * Gets an unmodifiable list of the names of the value's known fields.
     * @return The list
     */
    List<String> getFieldNames();

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

    /**
     * Returns true if this value can be used as a Nero Fact and false
     * otherwise.
     * @return true or false
     */
    default boolean isFact() {
        return false;
    }

    /**
     * If isFact(), returns this value as a Nero Fact.
     * @return The fact
     * @throws JoeError if !isFact().
     */
    default Fact toFact() {
        throw new UnsupportedOperationException(
            "Values of this type cannot be used as facts: '" +
            type().name() + "'.");
    }
}
