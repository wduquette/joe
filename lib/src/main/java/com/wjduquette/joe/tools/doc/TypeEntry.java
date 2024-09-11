package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

public class TypeEntry extends Entry {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The type's name.
    private final String name;

    private final List<ConstantEntry> constants = new ArrayList<>();
    private final List<StaticMethodEntry> staticMethods = new ArrayList<>();
    private final List<MethodEntry> methods = new ArrayList<>();

    private InitializerEntry initializer = null;

    //-------------------------------------------------------------------------
    // Constructor

    TypeEntry(PackageEntry pkg, String name) {
        super(pkg);
        this.name = name;
    }

    //-------------------------------------------------------------------------
    // Accessors

    String                  name()          { return name; }
    List<ConstantEntry>     constants()     { return constants; }
    List<StaticMethodEntry> staticMethods() { return staticMethods; }
    List<MethodEntry>       methods()       { return methods; }
    InitializerEntry        initializer()   { return initializer; }

    void setInitializer(InitializerEntry initializer) {
        this.initializer = initializer;
    }

}
