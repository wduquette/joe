package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * An entry for a global function that belongs to a package.
 */
class MethodEntry extends TypeMember implements Callable {
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

    public MethodEntry(TypeEntry type, String name) {
        super(type);

        this.name = name;
    }

    //-------------------------------------------------------------------------
    // Accessors

    public String prefix()        { return type().valuePrefix(); }
    public String name()          { return name; }
    public String id()            { return "method." + name; }
    public String fullMnemonic()  { return type().fullMnemonic() + "#" + id(); }
    public String shortMnemonic() { return type().shortMnemonic() + "#" + id(); }
    public String filename()      { return type().filename(); }
    public String returnSpec()    { return returnSpec; }

    public List<String> argSpecs()   { return argSpec; }

    public void setReturnSpec(String returnSpec) {
        this.returnSpec = returnSpec;
    }

    public String toString() {
        return "Method[" + name + "]";
    }
}
