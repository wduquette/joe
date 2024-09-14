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

    public String name()          { return name; }
    public String title()         { return title; }
    public String shortMnemonic() { return name; }
    public String fullMnemonic()  { return name; }
    public String filename()      { return "pkg." + name + ".md"; }

    List<FunctionEntry> functions() { return functions; }
    List<TypeEntry>     types()     { return types; }

    void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Package[" + name + "]";
    }

    //-------------------------------------------------------------------------
    // Computed properties

}
