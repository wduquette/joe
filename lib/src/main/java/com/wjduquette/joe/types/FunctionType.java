package com.wjduquette.joe.types;

import com.wjduquette.joe.*;
import com.wjduquette.joe.bert.BertCallable;

/**
 * The type proxy for Joe's `JoeCallable` type.
 */
public class FunctionType extends ProxyType<JoeCallable> {
    /**
     * The proxy's constant, used for installation.
     */
    public static final FunctionType TYPE = new FunctionType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe
    // @type Function
    // `Function` is the type of all Joe callables.

    /**
     * Creates an instance of the proxy.
     */
    public FunctionType() {
        super("Function");
        staticType();
        proxies(NativeCallable.class);
        proxies(BertCallable.class);

        method("toString", this::_toString);
    }

    //-------------------------------------------------------------------------
    // Instance Methods Implementations

    private Object _toString(JoeCallable func, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return func.toString();
    }

}
