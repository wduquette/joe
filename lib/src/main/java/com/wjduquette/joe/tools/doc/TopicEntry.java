package com.wjduquette.joe.tools.doc;

/**
 * An additional documentation topic for its parent entry.
 */
class TopicEntry extends Entry {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The topic's title
    private final String name;
    private String title;
    private final TypeEntry type;

    //-------------------------------------------------------------------------
    // Constructor

    public TopicEntry(PackageEntry pkg, String name) {
        super(pkg);

        this.type = null;
        this.name = name;
        this.title = name;
    }

    public TopicEntry(TypeEntry type, String name) {
        super(type.pkg());

        this.type = type;
        this.name = name;
        this.title = name;
    }

    //-------------------------------------------------------------------------
    // Accessors

    public void setTitle(String title) {
        this.title = title;
    }

    public String name()          { return name; }
    public String title()         { return title; }
    public String id()            { return "topic." + name; }
    public String fullMnemonic()  { return parent().fullMnemonic() + "#" + id(); }
    public String shortMnemonic() { return parent().shortMnemonic() + "#" + id(); }
    public String filename()      { return parent().filename(); }

    private Entry parent() {
        return type != null ? type : pkg();
    }

    public String toString() {
        return "Topic[" + name + "]";
    }
}
