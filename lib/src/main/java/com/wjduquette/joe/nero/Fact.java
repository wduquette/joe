package com.wjduquette.joe.nero;

import java.util.List;
import java.util.Map;

/**
 * Nero analyzes input Facts and infers output facts based on
 * {@link Rule Rules} in the Nero rule set.
 *
 * <p>A Fact might or might not have ordered fields that can be matched
 * positionally; all Facts must have fields that can be accessed by name.
 * Facts created by a Nero rule set will usually not have
 * explicit field names; in this case, the standard is to use field
 * names "f0", "f1", etc.</p>
 */
public sealed interface Fact
    permits ListFact, MapFact, PairFact
{
    /**
     * The fact's relation name.
     * @return The relation
     */
    String relation();

    /**
     * Gets whether the fact's fields can be accessed positionally via the
     * getFields() method or whether they must be accessed by name.
     * @return true or false
     */
    boolean isOrdered();

    /**
     * Returns the number of fields in the fact.
     * @return The number.
     */
    int arity();

    /**
     * If hasOrderedFields(), the list of field values.
     * @return the list
     * @throws IllegalStateException if !isIndexed.
     */
    default List<Object> getFields() {
        throw new IllegalStateException(
            "Fact does not have ordered fields!");
    }

    /**
     * Gets a map of the fact's field values by name.
     * @return The map
     */
    Map<String, Object> getFieldMap();

    /**
     * Gets the named field's value, or null if it's undefined.
     * @param name The field name
     * @return The value
     */
    default Object get(String name) {
        return getFieldMap().get(name);
    }
}
