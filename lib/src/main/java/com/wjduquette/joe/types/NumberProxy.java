package com.wjduquette.joe.types;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.TypeProxy;

public class NumberProxy extends TypeProxy<Double> {
    public static final NumberProxy TYPE = new NumberProxy();

    //-------------------------------------------------------------------------
    // Constructor

    public NumberProxy() {
        super("Number");
        staticType();

        constant("PI", Math.PI);

        staticMethod("abs", this::_abs);
    }

    //-------------------------------------------------------------------------
    // Static Methods

    private Object _abs(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "Number.abs(value)");
        return Math.abs(joe.toDouble(args.get(0)));
    }
}
