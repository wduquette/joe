package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * An entry for a global function that belongs to a package.
 */
public class InitializerEntry extends TypeMember implements Callable {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The callable's name.
    private final String name;

    // The callable's arg specs
    private final List<String> argSpec = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    public InitializerEntry(TypeEntry type, String name) {
        super(type);

        this.name = name;
    }

    //-------------------------------------------------------------------------
    // Accessors

    public String       name()          { return name; }
    public List<String> argSpecs()      { return argSpec; }
    public String returnSpec() { return type().name(); }

    public String toString() {
        return "Initializer[" + name + "]";
    }
}
