package com.wjduquette.joe.tools.doc;

abstract class TypeMember extends Entry {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The type of which this is a member
    private final TypeEntry type;

    //-------------------------------------------------------------------------
    // Constructor

    public TypeMember(TypeEntry type) {
        super(type.pkg());
        this.type = type;
    }

    //-------------------------------------------------------------------------
    // Accessors

    public TypeEntry type() { return type; }
}
