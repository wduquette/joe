package com.wjduquette.joe.tools.doc;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration data for `joe doc`
 */
class DocConfig {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final List<Path> inputFolders = new ArrayList<>();
    private final List<Path> inputFiles = new ArrayList<>();
    private Path outputFolder;

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
}
