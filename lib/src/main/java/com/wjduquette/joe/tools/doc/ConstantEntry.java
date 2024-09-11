package com.wjduquette.joe.tools.doc;

/**
 * An entry for a constant that belongs to a type.
 */
public class ConstantEntry extends TypeMember {
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

    public String name() { return name; }
}
