package com.wjduquette.joe;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A package containing Joe functions and or types, for installation into
 * a Joe interpreter.
 */
public class NativePackage extends JoePackage {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final List<NativeFunction> exportedFunctions = new ArrayList<>();
    private final List<NativeFunction> packageFunctions = new ArrayList<>();
    private final List<ProxyType<?>> exportedTypes = new ArrayList<>();
    private final List<ProxyType<?>> packageTypes = new ArrayList<>();
    private final List<ScriptResource> scriptResources = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the package, assigning its name.  The name should be a lowercase
     * dotted identifier, like a Java package name.
     * @param name The name.
     */
    public NativePackage(String name) {
        super(name);
    }

    //-------------------------------------------------------------------------
    // Operations

    /**
     * Loads the package's content into the engine, marking exports as exports.
     * @param joe The overall interpreter
     * @param engine The engine
     */
    public void load(Joe joe, Engine engine) {
        // Export exported functions
        for (var function : exportedFunctions) {
            export(engine, function.name(), function);
        }

        // Register and export exported types
        for (var type : exportedTypes) {
            joe.registerType(type);
            export(engine, type.name(), type);
        }

        // Install package functions
        for (var function : packageFunctions) {
            install(engine, function.name(), function);
        }

        // Register and install package types
        for (var type : packageTypes) {
            joe.registerType(type);
            install(engine, type.name(), type);
        }

        // Load script resources.
        scriptResources.forEach(r -> loadScriptResource(engine, r));
    }

    // Installs the value for private use
    private void install(Engine engine, String name, Object value) {
        engine.getEnvironment().setVariable(name, value);
    }

    // Installs and exports the value
    private void export(Engine engine, String name, Object value) {
        engine.getEnvironment().setVariable(name, value);
        engine.getExports().setVariable(name, value);
    }

    private void loadScriptResource(Engine engine, ScriptResource resource) {
        var cls = resource.cls;
        var name = resource.name;

        try (var stream = cls.getResourceAsStream(name)) {
            assert stream != null;
            var source = new String(stream.readAllBytes(),
                StandardCharsets.UTF_8);
            engine.run(name, source);
        } catch (SyntaxError ex) {
            throw new JoeError("Could not compile package resource '" +
                name + "' into package '" + name() + "':\n" +
                ex.getErrorReport());
        } catch (JoeError ex) {
            throw new JoeError("Could not execute package resource '" +
                name + "' into package '" + name() + "':\n" +
                ex.getJoeStackTrace());
        } catch (IOException ex) {
            throw new JoeError("Could not read package resource '" +
                name + "' for package '" + name() + "':\n" +
                ex.getMessage());
        }
    }

    //-------------------------------------------------------------------------
    // Package builders

    /**
     * Adds an exported function to the package.
     * @param name The function's name
     * @param joeLambda The callable, usually a method reference.
     */
    public final void function(String name, JoeLambda joeLambda) {
        exportedFunctions.add(new NativeFunction(name, "function", joeLambda));
    }

    /**
     * Adds an exported type to the package given its proxy.
     * @param proxyType The proxy.
     */
    public final void type(ProxyType<?> proxyType) {
        exportedTypes.add(proxyType);
    }

    /**
     * Adds a package-private function to the package.  Private functions
     * are for use by the package's scripts.
     * @param name The function's name
     * @param joeLambda The callable, usually a method reference.
     */
    @SuppressWarnings("unused")
    public final void packageFunction(String name, JoeLambda joeLambda) {
        packageFunctions.add(new NativeFunction(name, "function", joeLambda));
    }

    /**
     * Adds a package-private type to the package given its proxy.
     * Private types are for use by the package's scripts.  Values
     * of private types may be returned by exported functions.
     * @param proxyType The proxy.
     */
    @SuppressWarnings("unused")
    public final void packageType(ProxyType<?> proxyType) {
        packageTypes.add(proxyType);
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


    //-------------------------------------------------------------------------
    // Helper Classes

    private record ScriptResource(Class<?> cls, String name) {}
}
