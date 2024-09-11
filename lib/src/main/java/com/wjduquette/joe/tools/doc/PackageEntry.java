package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

public class PackageEntry extends Entry {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The package's name.
    private final String name;

    // The package's global functions.
    private final List<FunctionEntry> functions = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    public PackageEntry(String name) {
        super();
        this.name = name;
    }

    //-------------------------------------------------------------------------
    // Accessors

    public String              name()      { return name; }
    public List<FunctionEntry> functions() { return functions; }
}
