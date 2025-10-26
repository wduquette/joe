package com.wjduquette.joe.tools.doc;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration data for `joe doc`.  The configuration can be created
 * manually
 */
class DocConfig {
    // A source file to process.
    public record FilePair(Path sourceFile, Path destFile) {}

    //-------------------------------------------------------------------------
    // Instance Variables

    // The folder containing the `doc_config.joe` file.
    private final Path docConfigFolder;

    // The folder which will receive the finished HTML document produced
    // by the static site generator.
    private Path siteFolder;

    // Folder in which to find input files for expansion.
    private Path docInputFolder;

    // Folder in which to write the (possibly expanded) input files for
    // processing by the static site generator
    private Path docOutputFolder;

    // The folders and files to scan for JoeDoc comments
    private final List<Path> codeFolders = new ArrayList<>();
    private final List<Path> codeFiles = new ArrayList<>();

    // The library output folder, to received generated API docs
    private Path libOutputFolder;

    // Markdown files to process.
    private final List<FilePair> filePairs = new ArrayList<>();

    // Mapping from Java package names to javadoc package tree
    // prefix (an HTTP url or a path relative to the mdbook docs/
    // folder where the project's javadoc can be found).
    private final Map<String,String> javadocRoots = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the configuration, for population by executing the
     * doc_config.joe file.
     * @param docConfigFolder Folder containing the configuration file.
     */
    public DocConfig(Path docConfigFolder) {
        this.docConfigFolder = docConfigFolder;
    }

    //-------------------------------------------------------------------------
    // Accessors

    /**
     * Resolves the path relative to the location of the doc_config.joe
     * file.
     * @param path The relative path.
     * @return The resolved path.
     */
    public Path resolve(String path) {
        return docConfigFolder.resolve(path);
    }

    public Path docConfigFolder() {
        return docConfigFolder;
    }

    public Path siteFolder() {
        return siteFolder;
    }

    public void setSiteFolder(Path folder) {
        this.siteFolder = folder;
    }

    public Path docInputFolder() {
        return docInputFolder;
    }

    public void setDocInputFolder(Path folder) {
        this.docInputFolder = folder;
    }

    public Path docOutputFolder() {
        return docOutputFolder;
    }

    public void setDocOutputFolder(Path folder) {
        this.docOutputFolder = folder;
    }

    public List<Path> codeFolders() {
        return codeFolders;
    }

    public List<Path> codeFiles() {
        return codeFiles;
    }

    public Path libraryFolder() {
        return libOutputFolder;
    }

    public void setLibOutputFolder(Path folder) {
        this.libOutputFolder = folder;
    }

    public List<FilePair> filePairs() {
        return filePairs;
    }

    public Map<String,String> javadocRoots() {
        return javadocRoots;
    }
}
