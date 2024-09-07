package com.wjduquette.joe.types;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.TypeProxy;

import java.util.List;

public class StringProxy extends TypeProxy<String> {
    public static final StringProxy TYPE = new StringProxy();

    //-------------------------------------------------------------------------
    // Constructor

    public StringProxy() {
        super("String");

        proxies(String.class);
        initializer(this::_init);
        method("charAt",      this::_charAt);
        method("endsWith",    this::_endsWith);
        method("contains",    this::_contains);
        method("indent",      this::_indent);
//        method("indexOf",     this::_indexOf);  Wait for varargs
        method("length",      this::_length);
        method("startsWith",  this::_startsWith);
        method("stripIndent", this::_stripIndent);
    }

    //-------------------------------------------------------------------------
    // Initializer Implementation

    private Object _init(Joe joe, List<Object> args) {
        Joe.exactArity(args, 1, "String(value)");
        return joe.stringify(args.get(0));
    }

    //-------------------------------------------------------------------------
    // Method Implementations

    //**
    // @method charAt
    // @args index
    // @returns String
    // Returns the character at the *index* as a string.
    private Object _charAt(String value, Joe joe, List<Object> args) {
        Joe.exactArity(args, 1, "charAt(index)");
        var index = joe.toIndex(args.get(0), value.length());
        var c = value.charAt(index);
        return Character.toString(c);
    }

    //**
    // @method contains
    // @args target
    // @returns Boolean
    // Returns `true` if this contains the *target*, and `false` otherwise.
    private Object _contains(String value, Joe joe, List<Object> args) {
        Joe.exactArity(args, 1, "contains(target)");
        var target = joe.stringify(args.get(0));
        return value.contains(target);
    }

    //**
    // @method endsWith
    // @args suffix
    // @returns Boolean
    // Returns `true` if this string ends with the suffix, and `false` otherwise.
    private Object _endsWith(String value, Joe joe, List<Object> args) {
        Joe.exactArity(args, 1, "endsWith(suffix)");
        var suffix = joe.stringify(args.get(0));
        return value.endsWith(suffix);
    }

    //**
    // @method indent
    // @args n
    // @returns String
    // Indents or outdents the string by *n* characters.
    //
    // Note: Java's `String::indent` returns the result with a trailing
    // newline; this is easier to add than to remove, and is often unwanted,
    // so Joe trims it.
    private Object _indent(String value, Joe joe, List<Object> args) {
        Joe.exactArity(args, 1, "indent(n)");
        var n = joe.toInteger(args.get(0));
        return value.indent(n).stripTrailing();
    }

    //**
    // @method length
    // @returns Double
    // Gets the string's length.
    private Object _length(String value, Joe joe, List<Object> args) {
        Joe.exactArity(args, 0, "length()");
        return (double)value.length();
    }

    //**
    // @method startsWith
    // @args prefix
    // @returns Boolean
    // Returns `true` if this string starts with the prefix, and `false` otherwise.
    private Object _startsWith(String value, Joe joe, List<Object> args) {
        Joe.exactArity(args, 1, "startsWith(prefix)");
        var prefix = joe.stringify(args.get(0));
        return value.startsWith(prefix);
    }

    //**
    // @method stripIndent
    // @returns String
    // Strips the indent from each line of the string, preserving relative
    // indentation.
    private Object _stripIndent(String value, Joe joe, List<Object> args) {
        Joe.exactArity(args, 0, "stripIndent()");
        return value.stripIndent();
    }
}
