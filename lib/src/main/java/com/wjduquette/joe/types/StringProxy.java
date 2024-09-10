package com.wjduquette.joe.types;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.TypeProxy;

import java.util.Arrays;
import java.util.stream.Collectors;

public class StringProxy extends TypeProxy<String> {
    public static final StringProxy TYPE = new StringProxy();

    //-------------------------------------------------------------------------
    // Constructor

    public StringProxy() {
        super("String");

        proxies(String.class);
        initializer(this::_init);

        staticMethod("join",       this::_join);

        // pad/padLeft

        method("charAt",           this::_charAt);
        method("contains",         this::_contains);
        method("endsWith",         this::_endsWith);
        method("equalsIgnoreCase", this::_equalsIgnoreCase);
//        method("format",           this::_format);     // TODO Need printf logic
        method("indent",           this::_indent);
//        method("indexOf",          this::_indexOf);    // TODO Wait for varargs
        method("isBlank",          this::_isBlank);
        method("isEmpty",          this::_isEmpty);
//        method("lastIndexOf",      this::_lastIndexOf);  // TODO Wait for varargs
        method("length",           this::_length);
        method("lines",            this::_lines);
        method("matches",          this::_matches);
        method("repeat",           this::_repeat);
        method("replace",          this::_replace);
        method("replaceAll",       this::_replaceAll);
        method("replaceFirst",     this::_replaceFirst);
        method("split",            this::_split);
        method("startsWith",       this::_startsWith);
        method("strip",            this::_strip);
        method("stripIndent",      this::_stripIndent);
        method("stripLeading",     this::_stripLeading);
        method("stripTrailing",    this::_stripTrailing);
//        method("substring",        this::_substring);  // Wait for List
        method("toLowerCase",      this::_toLowerCase);
        method("toString",         this::_toString);
        method("toUpperCase",      this::_toUpperCase);
    }

    //-------------------------------------------------------------------------
    // Initializer Implementation

    private Object _init(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "String(value)");
        return joe.stringify(args.get(0));
    }

    //-------------------------------------------------------------------------
    // Static Method Implementations

    private Object _join(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 2, "join(delimiter, list)");
        var delim = joe.stringify(args.next());
        var list = joe.toList(args.next());
        return list.stream()
            .map(joe::stringify)
            .collect(Collectors.joining(delim));
    }


    //-------------------------------------------------------------------------
    // Method Implementations

    //**
    // @method charAt
    // @args index
    // @returns String
    // Returns the character at the *index* as a string.
    private Object _charAt(String value, Joe joe, ArgQueue args) {
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
    private Object _contains(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "contains(target)");
        var target = joe.stringify(args.get(0));
        return value.contains(target);
    }

    //**
    // @method endsWith
    // @args suffix
    // @returns Boolean
    // Returns `true` if this string ends with the suffix, and `false` otherwise.
    private Object _endsWith(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "endsWith(suffix)");
        var suffix = joe.stringify(args.get(0));
        return value.endsWith(suffix);
    }

    private Object _equalsIgnoreCase(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "other");
        return value.equalsIgnoreCase(joe.stringify(args.get(0)));
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
    private Object _indent(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "indent(n)");
        var n = joe.toInteger(args.get(0));
        return value.indent(n).stripTrailing();
    }

    private Object _isBlank(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "isBlank()");
        return value.isBlank();
    }

    private Object _isEmpty(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "isEmpty()");
        return value.isEmpty();
    }

    //**
    // @method length
    // @returns Double
    // Gets the string's length.
    private Object _length(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "length()");
        return (double)value.length();
    }

    //**
    // @method lines
    // @returns List
    // Returns a list consisting of the lines of text in the string.
    private Object _lines(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "lines()");
        return new ListValue(value.lines().toList());
    }

    private Object _matches(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "matches(pattern)");
        return value.matches(joe.toString(args.next()));
    }

    private Object _repeat(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "repeat(count)");
        var arg = args.next();
        var count = joe.toInteger(arg);

        if (count < 0) {
            throw joe.expected("non-negative count", arg);
        }

        return value.repeat(count);
    }

    private Object _replace(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 2, "replace(target,replacement)");
        return value.replace(
            joe.stringify(args.next()),
            joe.stringify(args.next())
        );
    }

    private Object _replaceAll(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 2, "replaceAll(regex, replacement)");
        return value.replaceAll(
            joe.toString(args.next()),
            joe.stringify(args.next())
        );
    }

    private Object _replaceFirst(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 2, "replaceFirst(regex, replacement)");
        return value.replaceFirst(
            joe.toString(args.next()),
            joe.stringify(args.next())
        );
    }

    private Object _split(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "split(delimiter)");
        var delim = joe.toString(args.next());
        var tokens = value.split(delim);
        return new ListValue(Arrays.asList(tokens));
    }

    //**
    // @method startsWith
    // @args prefix
    // @returns Boolean
    // Returns `true` if this string starts with the prefix, and `false` otherwise.
    private Object _startsWith(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "startsWith(prefix)");
        var prefix = joe.stringify(args.get(0));
        return value.startsWith(prefix);
    }

    private Object _strip(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "strip()");
        return value.strip();
    }

    //**
    // @method stripIndent
    // @returns String
    // Strips the indent from each line of the string, preserving relative
    // indentation.
    private Object _stripIndent(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "stripIndent()");
        return value.stripIndent();
    }

    private Object _stripLeading(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "stripLeading()");
        return value.stripLeading();
    }

    private Object _stripTrailing(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "stripTrailing()");
        return value.stripTrailing();
    }

    private Object _toLowerCase(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "toLowerCase()");
        return value.toLowerCase();
    }

    private Object _toString(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "toString()");
        return value;
    }

    private Object _toUpperCase(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "toUpperCase()");
        return value.toUpperCase();
    }

}
