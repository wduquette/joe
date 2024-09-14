package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * An entry for a global function that belongs to a package.
 */
public class StaticMethodEntry extends TypeMember implements Callable {
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

    public StaticMethodEntry(TypeEntry type, String name) {
        super(type);

        this.name = name;
    }

    //-------------------------------------------------------------------------
    // Accessors

    public String prefix() { return type().prefix(); }
    public String name() { return name; }
    public List<String> argSpecs() { return argSpec; }
    public String returnSpec() { return returnSpec; }

    public void setReturnSpec(String returnSpec) {
        this.returnSpec = returnSpec;
    }

    public String toString() {
        return "StaticMethod[" + name + "]";
    }
}
