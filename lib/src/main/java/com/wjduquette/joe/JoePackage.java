package com.wjduquette.joe;

/**
 * A package containing Joe functions and or types, for installation into
 * a Joe interpreter.
 */
public abstract class JoePackage {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The package's name.
    private final String name;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the package, assigning its name.  The name should be a lowercase
     * dotted identifier, like a Java package name.
     * @param name The name.
     */
    public JoePackage(String name) {
        this.name = name;
    }

    //-------------------------------------------------------------------------
    // Operations

    /**
     * The package subclass implements this to load the package into the
     * engine, managing the exports.
     * @param joe The Joe interpreter
     * @param engine The engine
     */
    abstract public void load(Joe joe, Engine engine);

    //-------------------------------------------------------------------------
    // Accessors

    /**
     * Gets the package's name.
     * @return The name
     */
    public String name() {
        return name;
    }
}
