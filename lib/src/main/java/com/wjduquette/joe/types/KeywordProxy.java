package com.wjduquette.joe.types;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.Keyword;
import com.wjduquette.joe.TypeProxy;

public class KeywordProxy extends TypeProxy<Keyword> {
    public static final KeywordProxy TYPE = new KeywordProxy();

    //-------------------------------------------------------------------------
    // Constructor

    public KeywordProxy() {
        super("Keyword");

        //**
        // @package joe
        // @type Keyword
        // A `Keyword` is a symbolic value, and one of the basic Joe types.
        // A keyword is an identifier with a leading hash symbol, e.g.,
        // `#flag`.  They are commonly used in Joe code in place of
        // enumerations, especially to identify options in variable length
        // argument lists.
        proxies(Keyword.class);
        initializer(this::_init);

        method("name",        this::_name);
        method("toString",    this::_toString);
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // @args name
    // Creates a new keyword given its name, without or without
    // the leading `#`.
    private Object _init(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "Keyword(name)");
        var arg = args.getRemaining(0);
        var name = joe.toString(arg);

        if (name.startsWith("#")) {
            name = name.substring(1);
        }

        if (!Joe.isIdentifier(name)) {
            throw joe.expected("keyword name", arg);
        }

        return new Keyword(name);
    }


    //-------------------------------------------------------------------------
    // Method Implementations

    //**
    // @method name
    // @result String
    // Gets the keyword's name, omitting the leading `#`.
    private Object _name(Keyword keyword, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "name()");
        return keyword.name();
    }

    //**
    // @method toString
    // @result String
    // Gets the keyword's string representation, including the leading `#`.
    private Object _toString(Keyword keyword, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "toString()");
        return keyword.toString();
    }
}
