package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

class PackageEntry extends Entry {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The package's name.
    private final String name;

    // The package's title
    private String title = null;

    // The package's global functions.
    private final List<FunctionEntry> functions = new ArrayList<>();

    // The package's types
    private final List<TypeEntry> types = new ArrayList<>();

    // The package's topics
    private final List<TopicEntry> topics = new ArrayList<>();


    //-------------------------------------------------------------------------
    // Constructor

    public PackageEntry(String name) {
        super();
        this.name = name;
    }

    public List<Entry> entries() {
        var result = new ArrayList<Entry>();

        result.addAll(functions);
        result.addAll(topics);

        for (var type : types) {
            result.add(type);
            result.addAll(type.constants());
            result.addAll(type.staticMethods());
            if (type.initializer() != null) {
                result.add(type.initializer());
            }
            result.addAll(type.methods());
            result.addAll(type.topics());
        }

        return result;
    }

    //-------------------------------------------------------------------------
    // Accessors

    public String name()          { return name; }
    public String title()         { return title; }
    public String shortMnemonic() { return name; }
    public String fullMnemonic()  { return name; }
    public String filename()      { return "pkg." + name + ".md"; }

    List<FunctionEntry> functions() { return functions; }
    List<TypeEntry>     types()     { return types; }
    List<TopicEntry>    topics()    { return topics; }

    void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Package[" + name + "]";
    }

    //-------------------------------------------------------------------------
    // Computed properties

}
