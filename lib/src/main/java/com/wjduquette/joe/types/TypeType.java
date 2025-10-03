package com.wjduquette.joe.types;

import com.wjduquette.joe.*;

/**
 * The "Type" type is the root of the Joe type system.  It has no
 * constructor. At the script level, `Type.of(Type) == Type`.
 */
public class TypeType extends ProxyType<Void> {
    /** The proxy's TYPE constant. */
    public static final TypeType TYPE = new TypeType();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an instance of the type.
     */
    public TypeType() {
        super("Type");

        //**
        // @package joe
        // @singleton Type
        // The `Type` type is the root of the Joe type system, being the
        // meta-type of all other types.  Instances of `Type` are types
        // themselves, and are created in a variety of ways, i.e.,
        // by declaring a `class`, or by implementing a Joe binding to a
        // Java type.
        staticType();
    }
}
