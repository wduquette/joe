package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

public class PackageEntry extends Entry {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The package's name.
    private final String name;

    // The package's title
    private String title = null;

    // The package's global functions.
    private final List<FunctionEntry> functions = new ArrayList<>();

    // The package's types
    private final List<TypeEntry> types = new ArrayList<>();


    //-------------------------------------------------------------------------
    // Constructor

    public PackageEntry(String name) {
        super();
        this.name = name;
    }

    //-------------------------------------------------------------------------
    // Accessors

    public String              name()      { return name; }
    public String              title()     { return title; }
    public List<FunctionEntry> functions() { return functions; }
    public List<TypeEntry>     types()     { return types; }

    public void setTitle(String title) {
        this.title = title;
    }

    public String toString() {
        return "Package[" + name + "]";
    }
}
