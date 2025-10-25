package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.AssertError;
import com.wjduquette.joe.ProxyType;

/**
 * The type proxy for {@link AssertError} values.
 */
public class AssertErrorType extends ProxyType<AssertError> {
    /** The proxy's TYPE constant. */
    public static final AssertErrorType TYPE = new AssertErrorType();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an instance of the proxy.
     */
    public AssertErrorType() {
        super("AssertError");

        //**
        // @package joe
        // @type AssertError
        // %extends Error
        // The `AssertError` type represents an exception thrown during the
        // execution of a `Joe` script by the `assert` statement, or
        // created by the `joe.test` package to represent a test value. A
        // script can catch errors thrown during execution using
        // the [[function.catch]] function.
        proxies(AssertError.class);
        extendsProxy(ErrorType.TYPE);

        initializer(this::_initializer);
    }

    @Override
    public String stringify(Joe joe, Object value) {
        assert value instanceof AssertError;
        var err = (AssertError)value;
        return "AssertError[type=" + err.getClass().getSimpleName()
            + ", message='" + err.getMessage() + "']";
    }

    //-------------------------------------------------------------------------
    // Initializer implementation

    //**
    // @init
    // %args message, [trace, ...]
    // Creates an `AssertError` with the given *message* and informational
    // trace messages.
    private Object _initializer(Joe joe, Args args) {
        args.minArity(1, "AssertError(message,[trace, ...])");
        var error = new AssertError(joe.stringify(args.next()));

        while (args.hasNext()) {
            error.addInfo(joe.stringify(args.next()));
        }

        return error;
    }
}
