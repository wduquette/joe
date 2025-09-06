package com.wjduquette.joe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Stores information about Joe's registered packages, including
 * whether they have been loaded or not and their exported symbols.
 */
public class PackageRegistry {
    /**
     * A Nero file containing local package information.
     */
    public static final String REPOSITORY_FILE = "repository.nero";

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

    //------------------------------------------------------------------------
    // Local Package Support

    /**
     * Searches libPath, a colon-delimited list of local folders, for
     * local Joe packages.  If verbose is true the details of the search
     * are written to the Joe output handler.
     * @param libPath The path
     * @param verbose true or false
     */
    public void findLocalPackages(String libPath, boolean verbose) {
        var folders = Arrays.stream(libPath.split(":"))
            .map(s -> Path.of(s).toAbsolutePath())
            .toList();
        for (var folder : folders) {
            if (verbose) joe.println("Searching folder: " + folder);
            findRepositories(folder, verbose);
        }
    }

    private void findRepositories(Path folder, boolean verbose) {
        try (var stream = Files.walk(folder, 10)) {
            var repos = stream
                .filter(p -> p.getFileName().toString().equals(REPOSITORY_FILE))
                .toList();
            for (var repo : repos) {
                if (verbose) joe.println("  Found: " + repo);
                findPackagesInRepository(repo, verbose);
            }
        } catch (IOException ex) {
            if (verbose) {
                joe.println("  Error reading folder: " + ex.getMessage());
            }
        }
    }

    private void findPackagesInRepository(Path repo, boolean verbose) {

    }
}
