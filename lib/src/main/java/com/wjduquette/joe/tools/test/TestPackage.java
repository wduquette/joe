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
        globalFunction("fail",         this::_fail);
        scriptResource(getClass(), "pkg.joe.test.joe");
        type(PathProxy.TYPE);
    }

    //**
    // @function assertEquals
    // @args got, expected
    // Verifies that *got* equals the *expected* value, producing an
    // informative assertion error if not.
    private Object _assertEquals(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 2, "assertEquals(got, expected)");
        var got = args.getRemaining(0);
        var expected = args.getRemaining(1);

        if (!Joe.isEqual(got, expected)) {
            throw new AssertError("Expected '" +
                joe.stringify(expected) + "', got: '" +
                joe.stringify(got) + "'.");
        }

        return null;
    }

    //**
    // @function fail
    // @args message
    // Throws an assertion error with the given *message*, failing the
    // test immediately.
    private Object _fail(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "fail(message)");
        throw new AssertError(joe.stringify(args.getRemaining(0)));
    }
}
