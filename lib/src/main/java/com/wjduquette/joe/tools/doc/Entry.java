package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * The documentation entry for a given entity.
 */
abstract class Entry {
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
        this.pkg = switch (this) {
            case PackageEntry p -> p;
            case MixinEntry ignored -> null;
            default ->
                throw new IllegalStateException(
                    "This entry type cannot use no-arg constructor");
        };
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

    public String id() { return null; } // Members only.

    abstract public String fullMnemonic();
    abstract public String shortMnemonic();
    abstract public String filename();
    public String url() {
        return id() != null
            ? filename() + "#" + id()
            : filename();
    }

    PackageEntry pkg()     { return pkg; }

    public List<String> content() { return content; }
}
