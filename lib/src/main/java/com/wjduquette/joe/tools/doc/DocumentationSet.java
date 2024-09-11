package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * The documentation entries scanned from the project source.
 */
public class DocumentationSet {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The packages in the documentation set.
    private final List<PackageEntry> packages = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    public DocumentationSet() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // Accessors

    public List<PackageEntry> packages() {
        return packages;
    }
}
