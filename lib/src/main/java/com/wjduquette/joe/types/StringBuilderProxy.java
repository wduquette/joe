package com.wjduquette.joe.types;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeList;
import com.wjduquette.joe.TypeProxy;

public class StringBuilderProxy extends TypeProxy<StringBuilder> {
    /**
     * The proxy's TYPE constant.
     */
    public static final StringBuilderProxy TYPE = new StringBuilderProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe
    // @type StringBuilder
    // A Joe `StringBuilder` is a Java `StringBuilder`, with a variety of
    // methods.

    /**
     * Creates the proxy.
     */
    public StringBuilderProxy() {
        super("StringBuilder");
        proxies(StringBuilder.class);
        initializer(this::_init);

        method("append",   this::_append);
        method("print",    this::_print);
        // TODO method("printf",   this::_printf);
        method("println",  this::_println);
        method("toString", this::_toString);
    }

    //-------------------------------------------------------------------------
    // Initializer Implementation

    //**
    // @init
    // Creates an empty `StringBuilder`.
    private Object _init(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "StringBuilder()");
        return new StringBuilder();
    }

    //-------------------------------------------------------------------------
    // Method Implementation

    //**
    // @method append
    // @args value
    // @result this
    // Adds the value to the buffer.
    private Object _append(StringBuilder buff, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "append(value)");
        buff.append(joe.stringify(args.next()));
        return buff;
    }

    //**
    // @method print
    // @args value
    // @result this
    // Adds the value to the buffer.
    private Object _print(StringBuilder buff, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "print(value)");
        buff.append(joe.stringify(args.next()));
        return buff;
    }

    //**
    // @method println
    // @args value
    // @result this
    // Adds the value to the buffer, followed by a new line.
    private Object _println(StringBuilder buff, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "println(value)");
        buff.append(joe.stringify(args.next())).append("\n");
        return buff;
    }

    //**
    // @method toString
    // @result String
    // Returns the string.
    private Object _toString(StringBuilder buff, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "toString()");
        return buff.toString();
    }
}
