package com.wjduquette.joe;

import java.util.Collection;

interface JoeObject {
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
}
