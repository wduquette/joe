package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.List;

/**
 * A package containing Joe functions and or types, for installation into
 * a Joe interpreter.
 */
public class Package {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final String name;

    private final List<NativeFunction> globalFunctions = new ArrayList<>();
    private final List<TypeProxy<?>> types = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the package, assigning its name.  The name should be a lowercase
     * dotted identifier, like a Java package name.
     * @param name The name.
     */
    public Package(String name) {
        this.name = name;
    }

    //-------------------------------------------------------------------------
    // Package builders

    /**
     * Adds a global function to the package.
     * @param name The function's name
     * @param callable The callable, usually a method reference.
     */
    public final void globalFunction(String name, JoeCallable callable) {
        globalFunctions.add(new NativeFunction(name, callable));
    }

    /**
     * Adds a registered type to the package, given its proxy.
     * @param typeProxy The proxy.
     */
    public final void type(TypeProxy<?> typeProxy) {
        types.add(typeProxy);
    }

    /**
     * Installs the package's native functions and types into the
     * engine.
     * @param joe The engine
     */
    public final void install(Joe joe) {
        globalFunctions.forEach(joe::installGlobalFunction);
        types.forEach(joe::installType);
    }

    //-------------------------------------------------------------------------
    // Accessors

    /**
     * Gets the package's name.
     * @return The name
     */
    @SuppressWarnings("unused")
    public String name() {
        return name;
    }
}
