package com.wjduquette.joe.tools.doc;

/**
 * An entry for JavaFX instance properties belonging to a type.
 */
class PropertyEntry extends TypeMember {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The property's name.
    private final String name;

    // The property's value type, or null
    private final String valueType;

    //-------------------------------------------------------------------------
    // Constructor

    public PropertyEntry(TypeEntry type, String name, String valueType) {
        super(type);

        this.name = name;
        this.valueType = valueType;
    }

    //-------------------------------------------------------------------------
    // Accessors

    public String name()          { return name; }
    public String valueType()     { return valueType; }
    public String id()            { return "property." + name; }
    public String fullMnemonic()  { return type().fullMnemonic() + "#" + id(); }
    public String shortMnemonic() { return type().shortMnemonic() + "#" + id(); }
    public String filename()      { return type().filename(); }
    public String url()           { return filename() + "#properties"; }

    public String toString() {
        return "Property[" + name + "]";
    }
}
