package com.wjduquette.joe.tools.doc;

import java.nio.file.Path;

/**
 * Configuration data for `joe doc`
 */
public class DocConfig {
    //-------------------------------------------------------------------------
    // Instance Variables

    private Path outputFolder;

    //-------------------------------------------------------------------------
    // Constructor

    public DocConfig() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // Configuration


    public Path getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(Path outputFolder) {
        this.outputFolder = outputFolder;
    }
}
