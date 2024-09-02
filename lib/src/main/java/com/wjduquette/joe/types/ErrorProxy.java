package com.wjduquette.joe.types;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.TypeProxy;

import java.util.List;

public class ErrorProxy extends TypeProxy<JoeError> {
    public static final ErrorProxy TYPE = new ErrorProxy();

    //-------------------------------------------------------------------------
    // Constructor

    public ErrorProxy() {
        super("Error");

        proxies(JoeError.class);
        method("message", this::_message);
    }

    //-------------------------------------------------------------------------
    // Method Implementations

    //**
    // @method message
    // @returns The message [[String]]
    // Gets the actual error message
    private Object _message(JoeError error, Joe joe, List<Object> args) {
        Joe.exactArity(args, 0, "message()");
        return error.getMessage();
    }
}
