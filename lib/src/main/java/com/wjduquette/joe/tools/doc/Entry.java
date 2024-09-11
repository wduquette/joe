package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * The documentation entry for a given entity.
 */
public abstract class Entry {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The entry's package
    private final PackageEntry pkg;

    // The entry's documentation text
    private final List<String> content = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Initializes the Entry presuming that it's a PackageEntry.
     */
    Entry() {
        if (this instanceof PackageEntry p) {
            pkg = p;
        } else {
            throw new IllegalStateException("Only PackageEntry can use no-arg constructor");
        }
    }

    /**
     * Initializes the entry with its PackageEntry.
     * @param pkg The package entry
     */
    Entry(PackageEntry pkg) {
        this.pkg = pkg;
    }

    //-------------------------------------------------------------------------
    // Accessors

    public PackageEntry pkg()     { return pkg; }
    public List<String> content() { return content; }
}
