package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * An entry for a global function that belongs to a package.
 */
class FunctionEntry extends Entry implements Callable {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The callable's name.
    private final String name;

    // The callable's arg specs
    private final List<String> argSpec = new ArrayList<>();

    // The callable's return value, or null
    private String returnSpec;

    //-------------------------------------------------------------------------
    // Constructor

    FunctionEntry(PackageEntry pkg, String name) {
        super(pkg);

        this.name = name;
    }

    //-------------------------------------------------------------------------
    // Accessors

    public String name()        { return name; }
    public String id()          { return "function." + name; }
    public String fullMnemonic()  { return pkg().fullMnemonic() + "#" + id(); }
    public String shortMnemonic() { return pkg().shortMnemonic() + "#" + id(); }
    public String filename()    { return pkg().filename(); }
    public String returnSpec()  { return returnSpec; }

    public List<String> argSpecs() { return argSpec; }

    void setReturnSpec(String returnSpec) {
        this.returnSpec = returnSpec;
    }

    public String toString() {
        return "Function[" + name + "]";
    }
}
