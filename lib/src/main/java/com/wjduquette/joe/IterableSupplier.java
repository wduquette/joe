package com.wjduquette.joe;

import java.util.Collection;

/**
 * A function for supplying a collection for iteration over a proxied
 * value.
 */
public interface IterableSupplier {
    /**
     * Given a value, produces a collection of items.
     * @param joe The Joe interpreter
     * @param value The value
     * @return The items
     */
    Collection<?> get(Joe joe, Object value);
}
