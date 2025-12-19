package com.wjduquette.joe;

import com.wjduquette.joe.nero.Nero;
import com.wjduquette.joe.nero.NeroDatabase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

/**
 * Searches for local-installed Joe packages given a library path.
 */
public class PackageFinder {
    /**
     * A Nero file containing local package information.
     */
    public static final String REPOSITORY_FILE = "repository.nero";

    //-------------------------------------------------------------------------
    // Static API

    /**
     * Finds packages on the given library path.
     * @param libPath The path
     * @return The finder
     */
    public static PackageFinder find(String libPath) {
        var finder = new PackageFinder();
        finder.findPackages(libPath, false);
        return finder;
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    // The finder needs a Joe, but it doesn't need anything special so
    // it creates its own.
    private final Joe joe;

    // The found packages, a map from package name to package
    private final Map<String,JoePackage> packages = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new package finder.
     */
    public PackageFinder() {
        this.joe = new Joe();
    }

    //-------------------------------------------------------------------------
    // Configuration

    /**
     * Gets the class's output handler, used for verbose output.
     * @return The handler.
     */
    @SuppressWarnings("unused")
    public Consumer<String> getOutputHandler() {
        return joe.getOutputHandler();
    }

    /**
     * Sets the class's output handler, used for verbose output.  The default
     * is System.out.print.
     * @param outputHandler The handler.
     */
    @SuppressWarnings("unused")
    public void setOutputHandler(Consumer<String> outputHandler) {
        joe.setOutputHandler(outputHandler);
    }


    //-------------------------------------------------------------------------
    // Queries

    /**
     * Gets the map of found packages by name.
     * @return The map
     */
    public Map<String,JoePackage> getPackages() {
        return Collections.unmodifiableMap(packages);
    }

    //------------------------------------------------------------------------
    // Operations

    /**
     * Searches libPath, a colon-delimited list of local folders, for
     * local Joe packages.  If verbose is true the details of the search
     * are written to the Joe output handler.
     * @param libPath The path
     * @param verbose true or false
     */
    public void findPackages(String libPath, boolean verbose) {
        if (libPath == null) {
            if (verbose) joe.println("No library path provided.");
            return;
        }
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
        NeroDatabase db;
        try {
            db = new NeroDatabase(joe).update("""
                    define ScriptedPackage/name, scriptFiles;
                    define JarPackage/name, jarFile, className;
                    """)
                .load(repo);
        } catch (JoeError ex) {
            if (verbose) {
                joe.println("    Error initializing package registry");
                joe.println(ex.getTraceReport().indent(6));
            }
            return;
        }

        var folder = repo.getParent();
        for (var f : db.all()) {
            var map = f.getFieldMap();
            JoePackage pkg = switch (f.relation()) {
                case "ScriptedPackage" -> {
                    if (verbose) joe.println("    " + Nero.toNeroAxiom(joe, f));
                    var pkgName = joe.toPackageName(map.get("name"));
                    var scriptFiles = joe.toList(map.get("scriptFiles"));
                    var paths = scriptFiles.stream()
                        .map(s -> folder.resolve(joe.stringify(s)))
                        .toList();
                    yield new ScriptedPackage(pkgName, folder, paths);
                }
                case "JarPackage" -> {
                    if (verbose) joe.println("    " + Nero.toNeroAxiom(joe, f));
                    var pkgName = joe.toPackageName(map.get("name"));
                    var jarFile = folder.resolve(joe.stringify(map.get("jarFile")));
                    var className = joe.stringify(map.get("className"));
                    yield new JarPackage(pkgName, folder, jarFile, className);
                }
                default -> {
                    if (verbose) joe.println("    Unexpected fact: " +
                        Nero.toNeroAxiom(joe, f));
                    yield null;
                }
            };

            if (pkg != null) {
                if (packages.containsKey(pkg.name())) {
                    if (verbose) joe.println(
                        "    Duplicate package name, skipping: '" +
                            pkg.name() + "'.");
                } else {
                    packages.put(pkg.name(), pkg);
                }
            }
        }
    }
}
