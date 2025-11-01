package com.wjduquette.joe;

import java.util.*;

/**
 * Stores information about Joe's registered packages, including
 * whether they have been loaded or not and their exported symbols.
 */
public class PackageRegistry {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The instance of Joe
    private final Joe joe;

    // The registry proper, a map from package name to package
    private final Map<String,JoePackage> registry = new HashMap<>();

    // Exports for loaded packages, a map from package name to
    // exports environment.
    private final Map<String,Environment> exportsMap = new HashMap<>();

    // Package loading stack: used to detect recursive loads.
    private final Stack<String> loadingStack = new Stack<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new package registry.  Intentionally package-private;
     * this is a subcomponent of Joe, and only Joe should be creating it.
     * @param joe The Joe interpreter
     */
    PackageRegistry(Joe joe) {
        this.joe = joe;
    }

    /**
     * Registers and loads the `joe` package into Joe's global environment.
     */
    void loadStandardLibrary() {
        var pkg = StandardLibrary.PACKAGE;
        if (isLoaded(pkg.name())) {
            throw new IllegalStateException(
                "The standard library has already been loaded!");
        }

        // FIRST, register the package
        registry.put(pkg.name(), pkg);

        // NEXT, load it into the global environment, which will be empty.
        pkg.load(joe, joe.engine());

        // NEXT, everything is exported, so copy the entire global environment
        // as the package's exports.
        exportsMap.put(StandardLibrary.PACKAGE.name(),
            new Environment(joe.engine().getEnvironment()));
    }

    //-------------------------------------------------------------------------
    // Operations

    /**
     * Registers all packages found by the package finder.
     * @param finder The finder
     */
    public void register(PackageFinder finder) {
        for (var pkg : finder.getPackages().values()) {
            register(pkg);
        }
    }

    /**
     * Registers the specific package.
     * @param pkg The package
     */
    public void register(JoePackage pkg) {
        registry.put(pkg.name(), pkg);
    }

    /**
     * Loads the named package.
     * @param pkgName The package name.
     * @throws JoeError if the package is not found.
     * @throws JoeError if the package has a recursive dependency.
     */
    public void load(String pkgName) {
        if (isLoaded(pkgName)) return;
        if (loadingStack.contains(pkgName)) {
            var ex = new JoeError("Recursive import of package '" + pkgName + "'.");
            for (var name : new ArrayList<>(loadingStack).reversed()) {
                ex.addFrame("From package '" + name + "'");
            }
            throw ex;
        }

        try {
            loadingStack.push(pkgName);
            var pkg = registry.get(pkgName);
            if (!hasPackage(pkgName)) {
                throw new JoeError("Unknown package: '" + pkgName + "'.");
            }

            var engine = joe.getVanillaEngine();
            pkg.load(joe, engine);
            exportsMap.put(pkgName, engine.getExports());
        } finally {
            loadingStack.pop();
        }
    }

    //-------------------------------------------------------------------------
    // Queries

    /**
     * Gets the known package names.
     * @return the names
     */
    @SuppressWarnings("unused")
    public Set<String> getPackageNames() {
        return registry.keySet();
    }

    /**
     * Returns true if the package is known, and false otherwise.
     * @param pkgName the name
     * @return true or false
     */
    public boolean hasPackage(String pkgName) {
        return registry.containsKey(pkgName);
    }

    /**
     * Gets the package by name
     * @param pkgName the name
     * @return The package or null
     */
    @SuppressWarnings("unused")
    public JoePackage getPackage(String pkgName) {
        return registry.get(pkgName);
    }

    /**
     * Returns true if the named package exists and has been loaded, and
     * false otherwise.
     * @param pkgName the package name
     * @return true or false
     */
    public boolean isLoaded(String pkgName) {
        return exportsMap.containsKey(pkgName);
    }

    /**
     * Gets an environment containing the named package's exported symbols.
     * @param pkgName the package name
     * @return the environment
     */
    public Environment getExports(String pkgName) {
        if (!isLoaded(pkgName)) {
            throw new IllegalStateException("Package not yet loaded: '" +
                pkgName + "'.");
        }
        return exportsMap.get(pkgName);
    }
}
