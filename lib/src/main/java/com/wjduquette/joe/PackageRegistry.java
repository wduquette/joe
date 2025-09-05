package com.wjduquette.joe;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    //-------------------------------------------------------------------------
    // Operations

    public void register(JoePackage pkg) {
        registry.put(pkg.name(), pkg);
    }

    public void load(String pkgName) {
        if (isLoaded(pkgName)) return;

        var pkg = registry.get(pkgName);
        if (!hasPackage(pkgName)) {
            throw new JoeError("Unknown package: '" + pkgName + "'.");
        }

        var engine = joe.getVanillaEngine();
        pkg.load(joe, engine);
        exportsMap.put(pkgName, engine.getExports());
    }

    //-------------------------------------------------------------------------
    // Queries

    public Set<String> getPackageNames() {
        return registry.keySet();
    }

    public boolean hasPackage(String pkgName) {
        return registry.containsKey(pkgName);
    }

    public JoePackage getPackage(String pkgName) {
        return registry.get(pkgName);
    }

    public boolean isLoaded(String pkgName) {
        return exportsMap.containsKey(pkgName);
    }

    public Environment getExports(String pkgName) {
        if (!isLoaded(pkgName)) {
            throw new IllegalStateException("Package not yet loaded: '" +
                pkgName + "'.");
        }
        return exportsMap.get(pkgName);
    }

}
