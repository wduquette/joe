package com.wjduquette.joe.types;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.TypeProxy;

import java.util.List;

public class StringProxy extends TypeProxy<String> {
    public static final StringProxy TYPE = new StringProxy();

    //-------------------------------------------------------------------------
    // Constructor

    public StringProxy() {
        super("String");

        proxies(String.class);
        method("length", this::_length);
    }

    //-------------------------------------------------------------------------
    // Method Implementations

    //**
    // @method length
    // @returns The string's length
    // Gets the string's length.
    private Object _length(String value, Joe joe, List<Object> args) {
        Joe.exactArity(args, 0, "length()");
        return (double)value.length();
    }
}
