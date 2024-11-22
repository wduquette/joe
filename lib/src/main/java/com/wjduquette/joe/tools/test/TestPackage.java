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
        globalFunction("assertError",  this::_assertError);
        globalFunction("assertFalse",  this::_assertFalse);
        globalFunction("assertTrue",   this::_assertTrue);
        globalFunction("fail",         this::_fail);
        globalFunction("skip",         this::_skip);
        scriptResource(getClass(), "pkg.joe.test.joe");
        type(PathProxy.TYPE);
    }

    //**
    // @function assertEquals
    // @args got, expected
    // Verifies that *got* equals the *expected* value, producing an
    // informative assertion error if not.
    private Object _assertEquals(Joe joe, Args args) {
        args.exactArity(2, "assertEquals(got, expected)");
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
    // @function assertError
    // @args callable, [message], [frames...]
    // Executes a *callable* expecting it to throw an error and
    // failing the test if it does not.  The error must have the
    // given *message* and stack *frames*, if they are provided.
    //
    // When stack *frames* are provided, the error must include at
    // least the provided number of frame strings, and those strings
    // must match.
    private Object _assertError(Joe joe, Args args) {
        args.minArity(1, "assertError(callable,[message],[frames...])");

        // FIRST, execute the callable and get the result or the error.
        Object result = null;
        JoeError error = null;
        try {
            result = joe.call(args.next());
        } catch (JoeError ex) {
            error = ex;
        }

        // NEXT, fail if we didn't get an error.
        if (error == null) {
            throw new AssertError("Expected error, got: " +
                joe.typedValue(result) + ".");
        }

        // NEXT, fail if the error message isn't as expected.
        if (args.hasNext()) {
            var message = joe.stringify(args.next());
            if (!Joe.isEqual(message, error.getMessage())) {
                throw new AssertError("Expected error message '" + message +
                    "', got: " + joe.typedValue(error.getMessage()) + ".");
            }
        }

        // NEXT, fail if a stack frame isn't as expected.
        // NOTE: we compare trace messages, but not the context span;
        // context often derives from the location of the test in the
        // test script, which is inherently fragile.
        var frames = args.remainderAsList();
        var traces = error.getTraces();

        for (int i = 0; i < frames.size(); i++) {
            if (i >= traces.size()) {
                throw new AssertError("Expected trace[" + i + "] == '" +
                    frames.get(i) + "', error has no matching trace.");
            } else if (!Joe.isEqual(frames.get(i), traces.get(i).message())) {
                throw new AssertError("Expected trace[" + i + "] == '" +
                    frames.get(i) + "', got: '" +
                    traces.get(i).message() + "'.");
            }
        }

        // NEXT, all is as expected.
        return null;
    }

    //**
    // @function assertFalse
    // @args condition
    // Verifies that *condition* is falsey, producing an
    // informative assertion error if not.
    private Object _assertFalse(Joe joe, Args args) {
        args.exactArity(1, "assertFalse(condition)");
        var condition = args.next();

        if (Joe.isTruthy(condition)) {
            throw new AssertError("Expected falsey value, got: " +
                joe.typedValue(condition) + ".");
        }

        return null;
    }

    //**
    // @function assertTrue
    // @args condition
    // Verifies that *condition* is truthy, producing an
    // informative assertion error if not.
    private Object _assertTrue(Joe joe, Args args) {
        args.exactArity(1, "assertTrue(condition)");
        var condition = args.next();

        if (!Joe.isTruthy(condition)) {
            throw new AssertError("Expected truthy value, got: " +
                joe.typedValue(condition) + ".");
        }

        return null;
    }

    //**
    // @function fail
    // @args message
    // Throws an assertion error with the given *message*, failing the
    // test immediately.
    private Object _fail(Joe joe, Args args) {
        args.exactArity(1, "fail(message)");
        throw new AssertError(joe.stringify(args.next()));
    }

    //**
    // @function skip
    // @args message
    // Skips the current test with the given message without executing it
    // further.  The test will be counted as "Skipped" in the final test
    // results.
    private Object _skip(Joe joe, Args args) {
        args.exactArity(1, "skip(message)");
        throw new TestTool.SkipError(joe.stringify(args.next()));
    }
}
