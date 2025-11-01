package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * An entry for a type initializer
 */
class InitializerEntry extends TypeMember implements Callable {
    private final String PREFIX = "init:";

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
    public String fullMnemonic()  { return PREFIX + type().fullMnemonic(); }
    public String shortMnemonic() { return PREFIX + type().shortMnemonic(); }
    public String filename()      { return type().filename(); }
    public String result()        { return type().name(); }

    public List<String> argSpecs() { return argSpec; }

    public String toString() {
        return "Initializer[" + name() + "]";
    }
}
