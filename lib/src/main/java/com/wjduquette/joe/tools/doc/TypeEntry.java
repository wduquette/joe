package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

class TypeEntry extends Entry {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The type's name.
    private final String name;

    private boolean isGeneric = false;
    private final List<ConstantEntry> constants = new ArrayList<>();
    private final List<StaticMethodEntry> staticMethods = new ArrayList<>();
    private String supertypeName = null;
    private String includes = null;
    private InitializerEntry initializer = null;
    private final List<MethodEntry> methods = new ArrayList<>();
    private final List<TopicEntry> topics = new ArrayList<>();


    //-------------------------------------------------------------------------
    // Constructor

    TypeEntry(PackageEntry pkg, String name) {
        super(pkg);
        this.name = name;
    }

    //-------------------------------------------------------------------------
    // Accessors

    public List<ConstantEntry>     constants()     { return constants; }
    public List<StaticMethodEntry> staticMethods() { return staticMethods; }
    public String                  supertypeName() { return supertypeName; }
    public String                  includes()      { return includes; }
    public InitializerEntry        initializer()   { return initializer; }
    public List<MethodEntry>       methods()       { return methods; }
    List<TopicEntry>               topics()        { return topics; }

    public String  prefix()        { return name; }
    public boolean isGeneric()     { return isGeneric; }
    public String  name()          { return name; }
    public String  fullMnemonic()  { return pkg().name() + "." + name; }
    public String  shortMnemonic() { return name; }
    public String  valuePrefix()   { return downCase(name); }
    public String  filename()      { return "type." + pkg().name() + "." + name + ".md"; }

    public void setGeneric(boolean flag) {
        this.isGeneric = flag;
    }

    public void setIncludes(String includes) {
        this.includes = includes;
    }

    public void setSupertypeName(String supertypeName) {
        this.supertypeName = supertypeName;
    }

    public void setInitializer(InitializerEntry initializer) {
        this.initializer = initializer;
    }

    @Override
    public String toString() {
        return "Type[" + name + "]";
    }

    private String downCase(String name) {
        if (!name.isEmpty()) {
            var ch = name.charAt(0);
            return Character.toLowerCase(ch) + name.substring(1);
        } else {
            return "";
        }
    }
}
