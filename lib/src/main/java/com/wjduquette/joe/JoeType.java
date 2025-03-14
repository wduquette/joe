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
}
