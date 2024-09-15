package com.wjduquette.joe.types;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.TypeProxy;

public class NumberProxy extends TypeProxy<Double> {
    public static final NumberProxy TYPE = new NumberProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe
    // @type Number
    // The `Number` type is a static type that collects together a number of
    // useful constants and numeric methods.
    public NumberProxy() {
        super("Number");
        staticType();
        proxies(Double.class);

        //**
        // @constant PI
        // The mathematical constant 𝛑.
        constant("PI", Math.PI);

        staticMethod("abs", this::_abs);
    }

    public String stringify(Joe joe, Object value) {
        assert value instanceof Double;

        String text = ((Double)value).toString();
        if (text.endsWith(".0")) {
            text = text.substring(0, text.length() - 2);
        }
        return text;
    }

    //-------------------------------------------------------------------------
    // Static Methods

    //**
    // @static abs
    // @args number
    // @result Number
    // Returns the absolute value of the given *number*.
    private Object _abs(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "Number.abs(value)");
        return Math.abs(joe.toDouble(args.get(0)));
    }
}
