package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.List;

/**
 * A package containing Joe functions and or types, for installation into
 * a Joe interpreter.
 */
public class JoePackage {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final String name;

    private final List<NativeFunction> globalFunctions = new ArrayList<>();
    private final List<TypeProxy<?>> types = new ArrayList<>();
    private final List<ScriptResource> scriptResources = new ArrayList<>();

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
    // Package builders

    /**
     * Adds a global function to the package.
     * @param name The function's name
     * @param callable The callable, usually a method reference.
     */
    public final void globalFunction(String name, JoeCallable callable) {
        globalFunctions.add(new NativeFunction(name, "function", callable));
    }

    /**
     * Adds a registered type to the package, given its proxy.
     * @param typeProxy The proxy.
     */
    public final void type(TypeProxy<?> typeProxy) {
        types.add(typeProxy);
    }

    /**
     * Adds a script resource file to the package given a Java class
     * and a resource file name relative to the location of that class.
     * @param cls The class
     * @param resource The resource file name.
     */
    public final void scriptResource(Class<?> cls, String resource) {
        scriptResources.add(new ScriptResource(cls, resource));
    }

    /**
     * Installs the package's native functions and types into the
     * engine.
     * @param joe The engine
     */
    public final void install(Joe joe) {
        globalFunctions.forEach(joe::installGlobalFunction);
        types.forEach(joe::installType);
        scriptResources.forEach(r -> joe.installScriptResource(r.cls, r.name));
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

    //-------------------------------------------------------------------------
    // Helper Classes

    private record ScriptResource(Class<?> cls, String name) {}
}
