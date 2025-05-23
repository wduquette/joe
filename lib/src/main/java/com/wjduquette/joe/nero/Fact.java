package com.wjduquette.joe.nero;

import java.util.List;
import java.util.Map;

/**
 * The Fact interface defines what {@link Nero} needs to know about
 * Fact objects in order to match {@link Rule rules} against them.
 * A Fact might or might not have indexed fields that can be matched
 * positionally; all Facts must have fields that can be accessed by name.
 *
 * <p>Facts created by a Nero rule set will often not have
 * explicit field names.  In this case, the standard is to use field
 * names "f0", "f1", etc.</p>
 */
public interface Fact {
    /**
     * The fact's relation name.
     * @return The relation
     */
    String relation();

    /**
     * Gets whether the fact's fields can be accessed positionally via the
     * fields() method or whether they must be accessed by name.
     * @return true or false
     */
    boolean isIndexed();

    /**
     * If isIndexed(), the list of field values.
     * @return the list
     * @throws IllegalStateException if !isIndexed.
     */
    default List<Object> fields() {
        throw new IllegalStateException("Fact does not have indexed fields!");
    }

    /**
     * Gets a map of the fact's field values by name.
     * @return The map
     */
    Map<String, Object> fieldMap();
}
