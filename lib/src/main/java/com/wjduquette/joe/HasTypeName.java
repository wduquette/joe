package com.wjduquette.joe;

/**
 * An interface for things that can report their type name to `Joe`.
 */
public interface HasTypeName {
    /**
     * Gets the script-level name of the value's type.
     * @return The name
     */
    String typeName();
}
