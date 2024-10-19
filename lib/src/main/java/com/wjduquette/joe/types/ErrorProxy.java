package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
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

        method("stackFrames", this::_stackFrames);
        method("stackTrace",  this::_stackTrace);
        method("message",     this::_message);
        method("type",        this::_type);
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
    // @args message, [frames...]
    // Creates an `Error` with the given *message*.
    private Object _initializer(Joe joe, Args args) {
        Joe.minArity(args, 1, "Error(message, [frames...])");
        var error = new JoeError(joe.stringify(args.next()));

        while (args.hasRemaining()) {
            error.getFrames().add(joe.stringify(args.next()));
        }

        return error;
    }

    //-------------------------------------------------------------------------
    // Method Implementations

    //**
    // @method stackFrames
    // @result List
    // Returns the list of stack frame strings.  Clients may add to the list
    // and rethrow the error.
    private Object _stackFrames(JoeError error, Joe joe, Args args) {
        Joe.exactArity(args, 0, "stackFrames()");
        return joe.wrapList(error.getFrames(), String.class);
    }

    //**
    // @method stackTrace
    // @result String
    // Returns the complete error, including the initial error messages
    // and all stack frames.
    private Object _stackTrace(JoeError error, Joe joe, Args args) {
        Joe.exactArity(args, 0, "stackTrace()");
        return error.getJoeStackTrace();
    }

    //**
    // @method message
    // @result text
    // Gets the actual error message
    private Object _message(JoeError error, Joe joe, Args args) {
        Joe.exactArity(args, 0, "message()");
        return error.getMessage();
    }

    //**
    // @method type
    // @result name
    // Gets the name of the concrete error type.
    private Object _type(JoeError error, Joe joe, Args args) {
        Joe.exactArity(args, 0, "type()");
        return error.getClass().getSimpleName();
    }
}
