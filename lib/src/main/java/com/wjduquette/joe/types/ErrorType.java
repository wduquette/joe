package com.wjduquette.joe.types;

import com.wjduquette.joe.*;

/**
 * The type proxy for {@link JoeError} values.
 */
public class ErrorType extends ProxyType<JoeError> {
    /** The proxy's TYPE constant. */
    public static final ErrorType TYPE = new ErrorType();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an instance of the proxy.
     */
    public ErrorType() {
        super("Error");

        //**
        // @package joe
        // @type Error
        // The `Error` type represents an exception thrown during the
        // execution of a `Joe` script. A script can catch errors
        // thrown during execution using the [[function.catch]] function.
        proxies(JoeError.class);

        initializer(this::_initializer);

        method("addInfo",         this::_addInfo);
        method("javaStackTrace",  this::_javaStackTrace);
        method("message",         this::_message);
        method("stackTrace",      this::_stackTrace);
        method("traces",          this::_traces);
        method("type",            this::_type);
    }

    @Override
    public String stringify(Joe joe, Object value) {
        assert value instanceof JoeError;
        var err = (JoeError)value;
        return "Error[type=" + err.getClass().getSimpleName()
            + ", message='" + err.getMessage() + "']";
    }

    //-------------------------------------------------------------------------
    // Initializer implementation

    //**
    // @init
    // @args message, [trace, ...]
    // Creates an `Error` with the given *message* and optional information
    // trace messages.
    private Object _initializer(Joe joe, Args args) {
        args.minArity(1, "Error(message, [trace, ...])");
        var error = new JoeError(joe.stringify(args.next()));

        while (args.hasNext()) {
            error.addInfo(joe.stringify(args.next()));
        }

        return error;
    }

    //-------------------------------------------------------------------------
    // Method Implementations

    //**
    // @method addInfo
    // @args message
    // @result this
    // Adds an information message to the list of traces.
    private Object _addInfo(JoeError error, Joe joe, Args args) {
        args.exactArity(1, "addInfo(message)");
        return error.addInfo(joe.stringify(args.next()));
    }

    //**
    // @method javaStackTrace
    // @result String
    // Returns the complete error, including the initial error messages
    // and all stack frames.
    private Object _javaStackTrace(JoeError error, Joe joe, Args args) {
        args.exactArity(0, "javaStackTrace()");
        return error.getJavaStackTrace();
    }

    //**
    // @method message
    // @result text
    // Gets the actual error message
    private Object _message(JoeError error, Joe joe, Args args) {
        args.exactArity(0, "message()");
        return error.getMessage();
    }

    //**
    // @method stackTrace
    // @result String
    // Returns the complete error, including the initial error messages
    // and all stack frames.
    private Object _stackTrace(JoeError error, Joe joe, Args args) {
        args.exactArity(0, "stackTrace()");
        return error.getJoeStackTrace();
    }

    //**
    // @method traces
    // @result List
    // Returns the list of trace strings.  Clients may add to the list
    // using [[Error#method.addInfo]].
    // and rethrow the error.
    private Object _traces(JoeError error, Joe joe, Args args) {
        args.exactArity(0, "traces()");
        return new ListValue(error.getTraces().stream()
            .map(Trace::message)
            .toList());
    }


    //**
    // @method type
    // @result name
    // Gets the name of the concrete error type.
    private Object _type(JoeError error, Joe joe, Args args) {
        args.exactArity(0, "type()");
        return error.getClass().getSimpleName();
    }
}
