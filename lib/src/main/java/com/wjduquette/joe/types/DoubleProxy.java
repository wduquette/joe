package com.wjduquette.joe.types;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.TypeProxy;

import java.util.List;

public class DoubleProxy extends TypeProxy<Double> {
    public static final DoubleProxy TYPE = new DoubleProxy();

    //-------------------------------------------------------------------------
    // Constructor

    public DoubleProxy() {
        super("Double");
        staticType();

        constant("PI", Math.PI);

        staticMethod("abs", this::_abs);
    }

    //-------------------------------------------------------------------------
    // Static Methods

    private Object _abs(Joe joe, List<Object> args) {
        Joe.exactArity(args, 1, "Double.abs(value)");
        return Math.abs(joe.toDouble(args.get(0)));
    }
}
