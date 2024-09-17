package com.wjduquette.joe.types;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.TypeProxy;

public class BooleanProxy extends TypeProxy<Boolean> {
    public static final BooleanProxy TYPE = new BooleanProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe
    // @type Boolean
    // The `Boolean` type has the expected values `true` and `false`.
    public BooleanProxy() {
        super("Boolean");
        staticType();
        proxies(Boolean.class);

        staticMethod("valueOf", this::_valueOf);
    }

    //-------------------------------------------------------------------------
    // Static Methods

    //**
    // @static valueOf
    // @args string
    // @result Boolean
    // Returns `true` if *string* equals the string `"true"`, ignoring case,
    // and `false` for all other values, including null.
    //
    // **Note:** this is consistent with the Java behavior.
    private Object _valueOf(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "Boolean.valueOf(value)");
        var arg = args.next();
        return arg != null && Boolean.parseBoolean(joe.toString(args));
    }
}
