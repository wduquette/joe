package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeType;
import com.wjduquette.joe.ProxyType;

public class JoeSingleton extends ProxyType<Void> {
    public static final JoeSingleton TYPE = new JoeSingleton();

    //-------------------------------------------------------------------------
    // Constructor

    public JoeSingleton() {
        super("Joe");

        //**
        // @package joe
        // @type Joe
        // The `Joe` singleton collects a variety of useful functions
        // related to the interpreter itself and to the installed
        // Joe types.
        staticType();

        staticMethod("compare",           this::_compare);
        staticMethod("currentTimeMillis", this::_currentTimeMillis);
        staticMethod("getFieldNames",     this::_getFieldNames);
        staticMethod("isOpaque",          this::_isOpaque);
        staticMethod("isType",            this::_isType);
        staticMethod("stringify",         this::_stringify);
        staticMethod("supertypeOf",       this::_supertypeOf);
        staticMethod("typeOf",            this::_typeOf);
    }

    //-------------------------------------------------------------------------
    // Static Method Implementations

    //**
    // @static compare
    // @args a, b
    // @result Number
    //
    // Given two strings or two numbers *a* and *b*, returns -1, 0,
    // or 1 as *a* < *b*, *a* == *b*, or *a* > *b*.  This function
    // is useful when sorting collections.
    private Object _compare(Joe joe, Args args) {
        args.exactArity(2, "compare(a, b)");
        return (double)Joe.compare(args.next(), args.next());
    }

    //**
    // @static currentTimeMillis
    // @result Number
    //
    // Returns Java's `System.currentTimeMillis()`, the current time in
    // milliseconds since the epoch.
    private Object _currentTimeMillis(Joe joe, Args args) {
        args.exactArity(0, "currentTimeMillis()");
        return (double)System.currentTimeMillis();
    }

    //**
    // @static getFieldNames
    // @args value
    // @result List
    // Returns a read-only list of the names of the fields defined on
    // the *value*.
    //
    // If the *value* has no fields, this method returns the empty list.
    //
    // If the *value* is a Joe type, this method returns the names of the
    // type's static fields.
    //
    // **Note**: There is no way to know the names of an
    // instance's fields just by looking at its type, generally speaking.
    // Distinct instances of the same Joe `class` might have different
    // fields.
    private Object _getFieldNames(Joe joe, Args args) {
        args.exactArity(1, "getFieldNames(value)");
        var value = joe.getJoeObject(args.next());
        return joe.readonlyList(value.getFieldNames());
    }

    //**
    // @static isOpaque
    // @args value
    // @result Boolean
    // Returns true if the *value* is of an opaque type, and false
    // otherwise.  An opaque type is a Java type with no
    // defined Joe binding.
    private Object _isOpaque(Joe joe, Args args) {
        args.exactArity(1, "isOpaque(value)");
        var obj = joe.getJoeObject(args.next());
        return obj.type() instanceof OpaqueType;
    }

    //**
    // @static isType
    // @args value
    // @result Boolean
    // Returns true if the *value* represents a Joe type, e.g.,
    // the [[String]] type or a Joe `class`, and false otherwise.
    private Object _isType(Joe joe, Args args) {
        args.exactArity(1, "isType(value)");
        return args.next() instanceof JoeType;
    }

    //**
    // @static stringify
    // @args value
    // @result String
    // Converts its value to a string for output.  This function
    // is functionally equivalent to [[String#init]], or to
    // `value.toString()` (if the type defines a `toString()` method).
    //
    // It is rare to need to call this function directly, but it is
    // available if needed.
    private Object _stringify(Joe joe, Args args) {
        args.exactArity(1, "stringify(value)");

        return joe.stringify(args.next(0));
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
    // Returns the Joe type of the given *value*.
    private Object _typeOf(Joe joe, Args args) {
        args.exactArity(1, "typeOf(value)");
        return joe.getJoeObject(args.next()).type();
    }
}
