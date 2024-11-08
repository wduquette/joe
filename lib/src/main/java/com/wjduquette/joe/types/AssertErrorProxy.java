package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.AssertError;
import com.wjduquette.joe.TypeProxy;

/**
 * The type proxy for {@link AssertError} values.
 */
public class AssertErrorProxy extends TypeProxy<AssertError> {
    /** The proxy's TYPE constant. */
    public static final AssertErrorProxy TYPE = new AssertErrorProxy();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an instance of the proxy.
     */
    public AssertErrorProxy() {
        super("AssertError");

        //**
        // @package joe
        // @type AssertError
        // @extends Error
        // The `AssertError` type represents an exception thrown during the
        // execution of a `Joe` script by the `assert` statement, or
        // created by the `joe.test` package to represent a test value. A
        // script can catch errors thrown during execution using
        // the [[function.catch]] function.
        proxies(AssertError.class);
        extendsProxy(ErrorProxy.TYPE);

        initializer(this::_initializer);
    }

    @Override
    public String stringify(Joe joe, Object value) {
        assert value instanceof AssertError;
        var err = (AssertError)value;
        return "AssertError[type=" + err.getClass().getSimpleName()
            + ", message='" + joe.codify(err.getMessage()) + "']";
    }

    //-------------------------------------------------------------------------
    // Initializer implementation

    //**
    // @init
    // @args message, [frames...]
    // Creates an `AssertError` with the given *message* and stack frame
    // strings.
    private Object _initializer(Joe joe, Args args) {
        args.minArity(1, "AssertError(message,[frames...])");
        var error = new AssertError(joe.stringify(args.next()));

        while (args.hasNext()) {
            error.getFrames().add(joe.stringify(args.next()));
        }

        return error;
    }
}
