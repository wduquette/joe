package com.wjduquette.joe.tools.doc;

/**
 * An entry for a constant that belongs to a type.
 */
class ConstantEntry extends TypeMember {
    private static final String PREFIX = "constant:";

    //-------------------------------------------------------------------------
    // Instance Variables

    // The type to which the constant

    // The constant's name.
    private final String name;

    // The constant's value type, or null
    private final String valueType;

    //-------------------------------------------------------------------------
    // Constructor

    public ConstantEntry(TypeEntry type, String name, String valueType) {
        super(type);

        this.name = name;
        this.valueType = valueType;
    }

    //-------------------------------------------------------------------------
    // Accessors

    public String name()          { return name; }
    public String valueType()     { return valueType; }
    public String id()            { return PREFIX + name; }
    public String fullMnemonic()  { return PREFIX + type().fullMnemonic() + "." + name; }
    public String shortMnemonic() { return PREFIX + type().shortMnemonic() + "." + name; }
    public String filename()      { return type().filename(); }
    public String url()           { return filename() + "#constants"; }

    public String toString() {
        return "Constant[" + name + "]";
    }
}
