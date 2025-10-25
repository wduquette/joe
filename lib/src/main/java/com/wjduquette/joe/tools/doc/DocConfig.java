package com.wjduquette.joe.tools.doc;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration data for `joe doc`
 */
class DocConfig {
    // A source file to process.
    public record FilePair(Path sourceFile, Path destFile) {}

    //-------------------------------------------------------------------------
    // Instance Variables

    // The folders and files to scan for JoeDoc comments
    private final List<Path> inputFolders = new ArrayList<>();
    private final List<Path> inputFiles = new ArrayList<>();

    // The library output folder
    private Path outputFolder;

    // Markdown files to process.
    private final List<FilePair> filePairs = new ArrayList<>();

    // Mapping from Java package names to javadoc package tree
    // prefix (an HTTP url or a path relative to the mdbook docs/
    // folder where the project's javadoc can be found).
    private final Map<String,String> javadocRoots = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    public DocConfig() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // Accessors

    public List<Path> inputFolders() {
        return inputFolders;
    }

    public List<Path> inputFiles() {
        return inputFiles;
    }

    public Path outputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(Path outputFolder) {
        this.outputFolder = outputFolder;
    }

    public List<FilePair> filePairs() {
        return filePairs;
    }

    public Map<String,String> javadocRoots() {
        return javadocRoots;
    }
}
