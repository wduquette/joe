package com.wjduquette.joe;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

/**
 * A package containing Joe functions and or types, for installation into
 * a Joe interpreter.
 */
public class JarPackage extends JoePackage {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Path folder;
    private final Path jarFile;
    private final String className;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the package, assigning its name and data. The name should be a
     * lowercase dotted identifier, like a Java package name.
     * @param name The name.
     * @param folder The disk folder in which the repository.nero file was found
     * @param jarFile The package's .jar file path
     * @param className The class name of a NativePackage in the .jar
     */
    public JarPackage(String name, Path folder, Path jarFile, String className) {
        super(name);
        this.folder = folder;
        this.jarFile = jarFile;
        this.className = className;
    }

    //-------------------------------------------------------------------------
    // Queries

    /**
     * Gets the folder containing the package's repository.nero file.
     * @return The folder
     */
    @SuppressWarnings("unused")
    public Path folder() {
        return folder;
    }

    /**
     * Gets the package's jar file.
     * @return The path
     */
    @SuppressWarnings("unused")
    public Path jarFile() {
        return jarFile;
    }

    /**
     * Gets the NativePackage's class name within the .jar file.
     * @return The name
     */
    @SuppressWarnings("unused")
    public String className() {
        return className;
    }

    //-------------------------------------------------------------------------
    // Operations

    /**
     * Loads the package's content into the engine, marking exports as exports.
     * @param joe The overall interpreter
     * @param engine The engine
     */
    @Override
    public void load(Joe joe, Engine engine) {
        URL jarUrl;

        try {
            jarUrl = jarFile.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new JoeError("Error loading Joe package '" + name() +
                "', malformed .jar file path: '" + jarFile + "'.");
        }
        URL[] urls = {jarUrl};

        try {
            // If we use try-with-resource, it will auto-close the class loader;
            // and then the NativePackage's code can't load additional classes
            // from the .jar at a later time.
            //noinspection resource
            URLClassLoader loader = new URLClassLoader(urls);
            Class<?> nativePkg = loader.loadClass(className);
            Object obj = nativePkg.getDeclaredConstructor().newInstance();
            if (obj instanceof NativePackage pkg) {
                pkg.load(joe, engine);
            } else {
                throw new JoeError("Error loading Joe package '" + name() +
                    "', from '" + jarFile + "', class '" +
                    className + "' is not a com.wjduquette.joe.NativePackage.");
            }
        } catch (JoeError ex) {
            throw ex;
        } catch (ClassNotFoundException e) {
            throw new JoeError("Error loading Joe package '" + name() +
                "', could not find class '" + className +
                "' in '" + jarFile + "'.");
        } catch (InvocationTargetException | InstantiationException |
            IllegalAccessException | NoSuchMethodException ex
        ) {
            throw new JoeError("Error loading Joe package '" + name() +
                "', could not create instance of '" + className +
                "' in '" + jarFile +
                "'; does it have a public no-arg constructor?");
        } catch (Exception ex) {
            throw new JoeError("Unexpected error loading Joe package '" + name() +
                "' from Java class '" + className + "':\n" + ex);
        }
    }
}
