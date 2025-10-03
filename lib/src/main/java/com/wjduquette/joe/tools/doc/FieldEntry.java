package com.wjduquette.joe.tools.doc;

/**
 * An entry for an instance field that belongs to a type.
 */
class FieldEntry extends TypeMember {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The field's name.
    private final String name;

    // The field's value type, or null
    private final String valueType;

    //-------------------------------------------------------------------------
    // Constructor

    public FieldEntry(TypeEntry type, String name, String valueType) {
        super(type);

        this.name = name;
        this.valueType = valueType;
    }

    //-------------------------------------------------------------------------
    // Accessors

    public String name()          { return name; }
    public String valueType()     { return valueType; }
    public String prefix()        { return type().valuePrefix(); }
    public String id()            { return "field." + name; }
    public String fullMnemonic()  { return type().fullMnemonic() + "#" + id(); }
    public String shortMnemonic() { return type().shortMnemonic() + "#" + id(); }
    public String filename()      { return type().filename(); }
    public String url()           { return filename() + "#fields"; }

    public String toString() {
        return "Field[" + name + "]";
    }
}
