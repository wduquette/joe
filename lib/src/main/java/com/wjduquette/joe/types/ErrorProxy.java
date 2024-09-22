package com.wjduquette.joe.types;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.TypeProxy;

/**
 * The type proxy for {@link JoeError} values.
 */
public class ErrorProxy extends TypeProxy<JoeError> {
    /** The proxy's TYPE constant. */
    public static final ErrorProxy TYPE = new ErrorProxy();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an instance of the proxy.
     */
    public ErrorProxy() {
        super("Error");

        //**
        // @package joe
        // @type Error
        // The `Error` type represents an exception thrown during the
        // execution of a `Joe` script. A script can catch errors
        // thrown during execution using the [[function.catch]] function.
        proxies(JoeError.class);

        initializer(this::_initializer);

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
    // Initializer implementation

    //**
    // @init
    // @args message
    // Creates an `Error` with the given *message*.
    private Object _initializer(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "Error(message)");
        return new JoeError(joe.stringify(args.next()));
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
