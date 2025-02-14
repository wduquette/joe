package com.wjduquette.joe;

/**
 * Interface for Java types that represent Joe types, especially those that
 * {@code canBeExtended()} by scripted classes.  A JoeClass provides the
 * type name and the type's methods table, and creates the Java instance
 * that represents values of the Joe type.
 */
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
     * Whether or not this class can be extended by a subclass.
     * @return true or false
     */
    default boolean canBeExtended() {
        return false;
    }

    /**
     * Creates an instance of the class.
     *
     * @param joe The interpreter
     * @param joeClass The actual parent class, either this class or a subclass.
     * @return The instance
     */
    default JoeObject make(Joe joe, JoeClass joeClass) {
        throw new UnsupportedOperationException();
    }
}
