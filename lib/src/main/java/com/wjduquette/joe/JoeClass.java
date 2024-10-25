package com.wjduquette.joe;

public interface JoeClass extends JoeCallable {
    /**
     * The name for a class's initializer method.
     */
    String INIT = "init";

    /**
     * Gets the class's name.
     * @return The name
     */
    String name();

    /**
     * Returns a callable that binds the named method
     * to the value, or null if the method was not found.
     * @param value The value
     * @param name The method name
     * @return The bound callable
     */
    JoeCallable bind(Object value, String name);

    /**
     * Whether or not this class can be subclassed.
     * @return true or false
     */
    default boolean canBeSubclassed() {
        return false;
    }
}
