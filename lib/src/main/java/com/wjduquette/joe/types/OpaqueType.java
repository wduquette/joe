package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.ProxyType;

/**
 * The type proxy for opaque native types.
 */
public class OpaqueType extends ProxyType<Object> {
    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an instance of the proxy.
     */
    public OpaqueType(Class<?> javaClass) {
        super(javaClass.getName());
        staticType();

        staticMethod("name", this::_name);

        method("toString",   this::_toString);
    }

    //-------------------------------------------------------------------------
    // Static Methods

    private Object _name(Joe joe, Args args) {
        args.exactArity(0, "name()");
        return name();
    }

    //-------------------------------------------------------------------------
    // Instance Methods

    private Object _toString(Object value, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return value.toString();
    }
}
