package com.wjduquette.joe.tools.test;

import com.wjduquette.joe.*;

class TestPackage extends JoePackage {
    public static final JoePackage PACKAGE = new TestPackage();

    // See pkg.joe.test.Joe for the remaining docs.

    //**
    // @package joe.test
    public TestPackage() {
        super("joe.test");
        globalFunction("assertEquals", this::_assertEquals);
        globalFunction("fail",         this::_fail);
        scriptResource(getClass(), "pkg.joe.test.joe");
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
