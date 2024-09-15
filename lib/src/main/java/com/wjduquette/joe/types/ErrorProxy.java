package com.wjduquette.joe.types;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.TypeProxy;

public class ErrorProxy extends TypeProxy<JoeError> {
    public static final ErrorProxy TYPE = new ErrorProxy();

    //-------------------------------------------------------------------------
    // Constructor

    public ErrorProxy() {
        super("Error");

        //**
        // @package joe
        // @type Error
        // The `Error` type represents an exception thrown during the
        // execution of a `Joe` script. A script can catch errors
        // thrown during execution using the [[function.catch]] function.
        proxies(JoeError.class);
        method("message", this::_message);
        method("type",    this::_type);
    }

    @Override
    public String stringify(Joe joe, Object value) {
        assert value instanceof JoeError;
        var err = (JoeError)value;
        return "Error[type=" + err.getClass().getSimpleName()
            + ", message='" + joe.codify(err.getMessage()) + "']";
    }

    //-------------------------------------------------------------------------
    // Method Implementations

    //**
    // @method message
    // @result text
    // Gets the actual error message
    private Object _message(JoeError error, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "message()");
        return error.getMessage();
    }

    //**
    // @method type
    // @result name
    // Gets the name of the concrete error type.
    private Object _type(JoeError error, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "type()");
        return error.getClass().getSimpleName();
    }
}
