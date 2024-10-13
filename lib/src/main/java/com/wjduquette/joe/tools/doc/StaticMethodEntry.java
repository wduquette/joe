package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * An entry for a global function that belongs to a package.
 */
class StaticMethodEntry extends TypeMember implements Callable {
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

    public StaticMethodEntry(TypeEntry type, String name) {
        super(type);

        this.name = name;
    }

    public StaticMethodEntry(TypeEntry type, StaticMethodEntry other) {
        super(type);
        this.name = other.name;
        this.argSpec.addAll(other.argSpec);
        this.result = other.result();
    }

    //-------------------------------------------------------------------------
    // Accessors

    public String prefix()        { return type().prefix(); }
    public String name()          { return name; }
    public String id()            { return "static." + name; }
    public String fullMnemonic()  { return type().fullMnemonic() + "#" + id(); }
    public String shortMnemonic() { return type().shortMnemonic() + "#" + id(); }
    public String filename()      { return type().filename(); }
    public String result()        { return result; }

    public List<String> argSpecs() { return argSpec; }

    public void setResult(String result) {
        this.result = result;
    }

    public String toString() {
        return "StaticMethod[" + name + "]";
    }
}
