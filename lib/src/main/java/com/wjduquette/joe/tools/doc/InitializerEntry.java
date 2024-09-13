package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * An entry for a type initializer
 */
public class InitializerEntry extends TypeMember implements Callable {
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

    public String       name()          { return type().name(); }
    public List<String> argSpecs()      { return argSpec; }
    public String returnSpec()          { return type().name(); }

    public String toString() {
        return "Initializer[" + name() + "]";
    }

    public String h3Title() { return name() + "()"; }
}
