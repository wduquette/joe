package com.wjduquette.joe;

/**
 * JoeType defines the introspection API for all Java classes that
 * represent Joe types at the script level, e.g., type proxies,
 * BertClass`, etc.
 */
public interface JoeType {
    /**
     * Gets the type's script-level name.
     * @return The name
     */
    String name();

    /**
     * Gets the type's supertype, or null if none.
     * @return the supertype.
     */
    default JoeType supertype() { return null; }

    /**
     * Gets whether this type has an immutable set of ordered fields,
     * and thus can be matched as a record rather than as a map.
     * The field values are typically also immutable, but the essential
     * thing is that no new fields can be added at runtime.
     * @return true or false
     */
    @SuppressWarnings("unused")
    default boolean isRecordType() { return false; }
}
