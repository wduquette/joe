package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * An entry for a type initializer
 */
class InitializerEntry extends TypeMember implements Callable {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The callable's arg specs
    private final List<String> argSpec = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    public InitializerEntry(TypeEntry type) {
        super(type);
    }

    //-------------------------------------------------------------------------
    // Accessors

    public String name()          { return type().name(); }
    public String id()            { return "init"; }
    public String fullMnemonic()  { return type().fullMnemonic() + "#" + id(); }
    public String shortMnemonic() { return type().shortMnemonic() + "#" + id(); }
    public String filename()      { return type().filename(); }

    public List<String> argSpecs()      { return argSpec; }
    public String returnSpec()          { return type().name(); }

    public String toString() {
        return "Initializer[" + name() + "]";
    }
}
