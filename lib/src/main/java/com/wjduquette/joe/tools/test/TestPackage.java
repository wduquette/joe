package com.wjduquette.joe.tools.test;

import com.wjduquette.joe.*;
import com.wjduquette.joe.console.PathProxy;

class TestPackage extends JoePackage {
    public static final JoePackage PACKAGE = new TestPackage();

    // See also pkg.joe.test.Joe for the rest of the package.

    //**
    // @package joe.test
    // @title Joe Test Tool API
    // This package defines the test assertions and checkers that are available
    // for use in `joe test` test suites.
    //
    // ## Imported Types
    //
    // In addition to the APIs documented here, `joe.test` includes types from
    // other packages so that they can be tested:
    //
    // - From [[joe.console]], [[joe.console.Path]]

    public TestPackage() {
        super("joe.test");
        globalFunction("assertEquals", this::_assertEquals);
        globalFunction("assertTrue",   this::_assertTrue);
        globalFunction("assertFalse",  this::_assertFalse);
        globalFunction("catchError",   this::_catchError);
        globalFunction("fail",         this::_fail);
        scriptResource(getClass(), "pkg.joe.test.joe");
        type(PathProxy.TYPE);
    }

    //**
    // @function assertEquals
    // @args got, expected
    // Verifies that *got* equals the *expected* value, producing an
    // informative assertion error if not.
    private Object _assertEquals(Joe joe, Args args) {
        Joe.exactArity(args, 2, "assertEquals(got, expected)");
        var got      = args.next();
        var expected = args.next();

        if (!Joe.isEqual(got, expected)) {
            throw new AssertError("Expected " +
                joe.typedValue(expected) + ", got: " +
                joe.typedValue(got) + ".");
        }

        return null;
    }

    //**
    // @function assertTrue
    // @args condition
    // Verifies that *condition* is truthy, producing an
    // informative assertion error if not.
    private Object _assertTrue(Joe joe, Args args) {
        Joe.exactArity(args, 1, "assertTrue(condition)");
        var condition = args.next();

        if (!Joe.isTruthy(condition)) {
            throw new AssertError("Expected truthy value, got: " +
                joe.typedValue(condition) + ".");
        }

        return null;
    }

    //**
    // @function assertFalse
    // @args condition
    // Verifies that *condition* is falsey, producing an
    // informative assertion error if not.
    private Object _assertFalse(Joe joe, Args args) {
        Joe.exactArity(args, 1, "assertFalse(condition)");
        var condition = args.next();

        if (Joe.isTruthy(condition)) {
            throw new AssertError("Expected falsey value, got: " +
                joe.typedValue(condition) + ".");
        }

        return null;
    }

    //**
    // @function catchError
    // @args callable
    // @result String
    // This simplified version of the standard
    // [[joe#function.catch]] function executes the *callable* and catches
    // the resulting [[joe.Error]], returning the error's message or.  The
    // test fails if the *callable* does not throw an error.
    private Object _catchError(Joe joe, Args args) {
        Joe.exactArity(args, 1, "catchError(callable)");
        Object result;
        try {
            result = joe.call(args.next());
        } catch (JoeError ex) {
            return ex.getMessage();
        }

        throw joe.expected("error", result);
    }

    //**
    // @function fail
    // @args message
    // Throws an assertion error with the given *message*, failing the
    // test immediately.
    private Object _fail(Joe joe, Args args) {
        Joe.exactArity(args, 1, "fail(message)");
        throw new AssertError(joe.stringify(args.next()));
    }
}
