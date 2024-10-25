package com.wjduquette.joe;

public interface JoeClass extends JoeCallable {
    /**
     * Gets the class's name.
     * @return The name
     */
    String name();

    /**
     * Finds an instance method, by name.
     * @param name The method name
     * @return The bound function
     */
    JoeFunction findMethod(String name);

    /**
     * Whether or not this class can be subclassed.
     * @return true or false
     */
    default boolean canSubclass() {
        return false;
    }
}
