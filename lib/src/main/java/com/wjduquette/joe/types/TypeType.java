package com.wjduquette.joe.types;

import com.wjduquette.joe.*;

/**
 * The "Type" type is the root of the Joe type system.  It is a
 * static type, and hosts the type introspection API.  Its own
 * At the script level, `Type.of(Type) == Type`.
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
        // @type Type
        // The `Type` type is the root of the Joe type system, being the
        // meta-type of all other types.  It hosts Joe's
        // type and value introspection API.
        staticType();

        staticMethod("fieldNames",    this::_fieldNames);
        staticMethod("isOpaqueValue", this::_isOpaqueValue);
        staticMethod("isType",        this::_isType);
        staticMethod("name",          this::_name);
        staticMethod("nameOf",        this::_nameOf);
        staticMethod("supertypeOf",   this::_supertypeOf);
        staticMethod("typeOf",        this::_typeOf);
    }

    //-------------------------------------------------------------------------
    // Static Method Implementations

    //**
    // @static fieldNames
    // @args value
    // @result List
    // Returns a list of the names of the fields defined on the *value*.
    //
    // - If the *value* is a Joe type, returns the names of the type's
    //   static fields.
    // - Instances of the same Joe `class` might have different fields.
    // - If the *value* has no fields, returns the empty list.
    private Object _fieldNames(Joe joe, Args args) {
        args.exactArity(1, "fieldNames(value)");
        var value = joe.getJoeObject(args.next());
        return value.getFieldNames();
    }

    //**
    // @static isOpaqueValue
    // @args value
    // @result Boolean
    // Returns true if the *value* is of an opaque type, and false
    // otherwise.  An opaque type is a Java type with no
    // defined Joe binding.
    private Object _isOpaqueValue(Joe joe, Args args) {
        args.exactArity(1, "isOpaqueValue(value)");
        var obj = joe.getJoeObject(args.next());
        return obj.type() instanceof OpaqueType;
    }

    //**
    // @static isType
    // @args value
    // @result Boolean
    // Returns true if the *value* represents a Joe type, e.g.,
    // the [[String]] type, and false otherwise.
    private Object _isType(Joe joe, Args args) {
        args.exactArity(1, "isType(value)");
        return args.next() instanceof JoeType;
    }

    //**
    // @static name
    // @result String
    // Returns the name of this type, i.e., "Type".
    private Object _name(Joe joe, Args args) {
        args.exactArity(0, "name()");
        return name();
    }

    //**
    // @static nameOf
    // @args type
    // @result String
    // Returns the name of the given *type*.
    private Object _nameOf(Joe joe, Args args) {
        args.exactArity(1, "nameOf(type)");
        var type = joe.toJoeType(args.next());
        return type.name();
    }

    //**
    // @static supertypeOf
    // @args type
    // @result type
    // Returns the supertype of the given *type*, or null if the *type*
    // has no supertype.
    private Object _supertypeOf(Joe joe, Args args) {
        args.exactArity(1, "supertypeOf(type)");
        var type = joe.toJoeType(args.next());
        return type.supertype();
    }

    //**
    // @static typeOf
    // @args value
    // @result type
    // Returns the Joe type of this *value*.
    private Object _typeOf(Joe joe, Args args) {
        args.exactArity(1, "typeOf(value)");
        return joe.getJoeObject(args.next()).type();
    }
}
