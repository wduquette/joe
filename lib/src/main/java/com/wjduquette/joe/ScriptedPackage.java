package com.wjduquette.joe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A package containing Joe functions and or types, for installation into
 * a Joe interpreter.
 */
public class ScriptedPackage extends JoePackage {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Path folder;
    private final List<Path> scriptFiles = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the package, assigning its name and data. The name should be a
     * lowercase dotted identifier, like a Java package name.
     * @param name The name.
     * @param folder The disk folder in which the repository.nero file was found
     * @param scriptFiles The package's source files, in load order
     */
    public ScriptedPackage(String name, Path folder, List<Path> scriptFiles) {
        super(name);
        this.folder = folder;
        this.scriptFiles.addAll(scriptFiles);
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
     * Gets the list of the package's script files, in load order.
     * @return The list
     */
    @SuppressWarnings("unused")
    public List<Path> scriptFiles() {
        return Collections.unmodifiableList(scriptFiles);
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
        // Load script files.
        scriptFiles.forEach(p -> loadScriptFile(engine, p));
    }

    private void loadScriptFile(Engine engine, Path path) {
        String script;

        try {
            script = Files.readString(path);
            engine.run(path.getFileName().toString(), script);
        } catch (SyntaxError ex) {
            throw new JoeError("Could not compile package script '" +
                path + "' into package '" + name() + "':\n" +
                ex.getErrorReport());
        } catch (JoeError ex) {
            throw new JoeError("Could not execute package script '" +
                path + "' into package '" + name() + "':\n" +
                ex.getJoeStackTrace());
        } catch (IOException ex) {
            throw new JoeError("Could not read package script '" +
                path + "' for package '" + name() + "':\n" +
                ex.getMessage());
        }
    }
}
