package com.wjduquette.joe.tools.doc;

/**
 * An entry for a constant that belongs to a type.
 */
class ConstantEntry extends TypeMember {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The type to which the constant

    // The constant's name.
    private final String name;

    //-------------------------------------------------------------------------
    // Constructor

    public ConstantEntry(TypeEntry type, String name) {
        super(type);

        this.name = name;
    }

    //-------------------------------------------------------------------------
    // Accessors

    public String name()     { return name; }
    public String id()       { return "constant." + name; }
    public String fullMnemonic()  { return type().fullMnemonic() + "#" + id(); }
    public String shortMnemonic() { return type().shortMnemonic() + "#" + id(); }
    public String filename() { return type().filename(); }

    public String toString() {
        return "Constant[" + name + "]";
    }
}
