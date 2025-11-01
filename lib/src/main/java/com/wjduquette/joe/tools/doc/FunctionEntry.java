package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * An entry for a global function that belongs to a package.
 */
class FunctionEntry extends Entry implements Callable {
    private static final String PREFIX = "function:";

    //-------------------------------------------------------------------------
    // Instance Variables

    // The callable's name.
    private final String name;

    // The callable's arg specs
    private final List<String> argSpec = new ArrayList<>();

    // The callable's return value, or null
    private String result;

    //-------------------------------------------------------------------------
    // Constructor

    FunctionEntry(PackageEntry pkg, String name) {
        super(pkg);

        this.name = name;
    }

    //-------------------------------------------------------------------------
    // Accessors

    public String name()          { return name; }
    public String id()            { return PREFIX + name; }
    public String fullMnemonic()  { return PREFIX + pkg().fullMnemonic() + "." + name; }
    public String shortMnemonic() { return id(); }
    public String filename()      { return pkg().filename(); }
    public String result()        { return result; }

    public List<String> argSpecs() { return argSpec; }

    void setResult(String result) {
        this.result = result;
    }

    public String toString() {
        return "Function[" + name + "]";
    }
}
