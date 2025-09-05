package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.List;

/**
 * A package containing Joe functions and or types, for installation into
 * a Joe interpreter.
 */
public abstract class JoePackage {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The package's name.
    private final String name;

    // The package's exports.
    private Environment exports = null;

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
     * Loads the package
     * @param joe The Joe interpreter
     */
    public void load(Joe joe) {
        if (isLoaded()) return;

        var engine = joe.getVanillaEngine();
        loadPackage(joe, engine);
        exports = engine.getExports();
    }

    /**
     * The package subclass implements this to load the package into the
     * engine, managing the exports.
     * @param joe The Joe interpreter
     * @param engine The engine
     */
    abstract protected void loadPackage(Joe joe, Engine engine);

    //-------------------------------------------------------------------------
    // Accessors

    /**
     * Gets the package's name.
     * @return The name
     */
    public String name() {
        return name;
    }

    /**
     * The package has been loaded if it has been given exports.
     * @return true or false
     */
    public boolean isLoaded() {
        return exports != null;
    }

    /**
     * Returns the package's exports if isLoaded(), and null otherwise.
     * @return The exports or null.
     */
    public Environment getExports() {
        return exports;
    }
}
