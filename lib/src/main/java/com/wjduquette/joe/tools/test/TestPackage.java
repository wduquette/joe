package com.wjduquette.joe.tools.test;

import com.wjduquette.joe.*;
import com.wjduquette.joe.console.PathProxy;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The package that defines the test API for test scripts.
 */
public class TestPackage extends NativePackage {
    // See also pkg.joe.test.Joe for the rest of the package.

    //-------------------------------------------------------------------------
    // Instance Variables

    // The name of the engine in use.
    private final String engine;

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.test
    // %title Joe Test Tool API
    // This package defines the test assertions and checkers that are available
    // for use in `joe test` test suites.
    //
    // ## Imported Types
    //
    // In addition to the APIs documented here, `joe.test` includes types from
    // other packages so that they can be tested:
    //
    // - From [[joe.console]], [[joe.console.Path]]

    /**
     * Creates the package.
     * @param engine The name of the language engine in use, e.g., BERT or WALKER.
     */
    public TestPackage(String engine) {
        super("joe.test");
        this.engine = engine;

        function("assertEQ",     this::_assertEQ);
        function("assertError",  this::_assertError);
        function("assertF",  this::_assertF);
        function("assertT",   this::_assertT);
        function("engine",       this::_engine);
        function("fail",         this::_fail);
        function("skip",         this::_skip);
        function("typedValue",   this::_typedValue);

        scriptResource(getClass(), "pkg.joe.test.joe");

        type(new JoeTestType());
        type(PathProxy.TYPE);
    }

    //**
    // @function assertEQ
    // %args got, expected
    // Verifies that *got* equals the *expected* value, producing an
    // informative assertion error if not.
    private Object _assertEQ(Joe joe, Args args) {
        args.exactArity(2, "assertEQ(got, expected)");
        var got      = args.next();
        var expected = args.next();

        if (!Joe.isEqual(got, expected)) {
            throw new AssertError(
                "Computed: " + testValue(joe, got) +
                "\nExpected: " + testValue(joe, expected));
        }

        return null;
    }

    //**
    // @function assertError
    // %args callable, [message], [frames...]
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
    // @function assertF
    // %args condition
    // Verifies that *condition* is falsey, producing an
    // informative assertion error if not.
    private Object _assertF(Joe joe, Args args) {
        args.exactArity(1, "assertF(condition)");
        var condition = args.next();

        if (Joe.isTruthy(condition)) {
            throw new AssertError("Expected falsey value, got: " +
                joe.typedValue(condition) + ".");
        }

        return null;
    }

    //**
    // @function assertT
    // %args condition
    // Verifies that *condition* is truthy, producing an
    // informative assertion error if not.
    private Object _assertT(Joe joe, Args args) {
        args.exactArity(1, "assertT(condition)");
        var condition = args.next();

        if (!Joe.isTruthy(condition)) {
            throw new AssertError("Expected truthy value, got: " +
                joe.typedValue(condition) + ".");
        }

        return null;
    }

    //**
    // @function engine
    // %result String
    // Returns the name of the engine in use, "walker" or "bert".
    private Object _engine(Joe joe, Args args) {
        args.exactArity(0, "engine()");
        return engine;
    }

    //**
    // @function fail
    // %args message
    // Throws an assertion error with the given *message*, failing the
    // test immediately.
    private Object _fail(Joe joe, Args args) {
        args.exactArity(1, "fail(message)");
        throw new AssertError(joe.stringify(args.next()));
    }

    //**
    // @function skip
    // %args message
    // Skips the current test with the given message without executing it
    // further.  The test will be counted as "Skipped" in the final test
    // results.
    private Object _skip(Joe joe, Args args) {
        args.exactArity(1, "skip(message)");
        throw new TestTool.SkipError(joe.stringify(args.next()));
    }

    //**
    // @function typedValue
    // %args value
    // Outputs the value's type and value for display.  Collections are output in
    // readable format.
    private Object _typedValue(Joe joe, Args args) {
        args.exactArity(1, "typedValue(value)");
        return testValue(joe, args.next());
    }


    //------------------------------------------------------------------------
    // Helpers

    private String testValue(Joe joe, Object value) {
        return switch (value) {
            case List<?> c -> typedList(joe, c);
            case Set<?> c -> typedSet(joe, c);
            case Map<?,?> m -> typedMap(joe, m);
            default -> joe.typedValue(value);
        };
    }

    private String typedList(Joe joe, List<?> value) {
        var items = value.stream()
            .map(joe::typedValue)
            .collect(Collectors.joining("\n"));
        if (value.isEmpty()) {
            return joe.typeName(value) + " []";
        } else if (value.size() == 1) {
            return joe.typeName(value) + " [" + items + "]";
        } else {
            return joe.typeName(value) + " [\n" + items.indent(4) + "]";
        }
    }
    private String typedSet(Joe joe, Set<?> value) {
        var items = value.stream()
            .map(joe::typedValue)
            .sorted()
            .collect(Collectors.joining("\n"));
        if (value.isEmpty()) {
            return joe.typeName(value) + " {}";
        } else if (value.size() == 1) {
            return joe.typeName(value) + " {" + items + "}";
        } else {
            return joe.typeName(value) + " {\n" + items.indent(4) + "}";
        }
    }

    private String typedMap(
        Joe joe,
        Map<?,?> map
    ) {
        var items = map.entrySet().stream()
            .map(e -> joe.typedValue(e.getKey()) + ": " +
                joe.typedValue(e.getValue()))
            .collect(Collectors.joining("\n"));
        if (map.isEmpty()) {
            return joe.typeName(map) + " {:}";
        } else if (map.size() == 1) {
            return joe.typeName(map) + " {" + items + "}";
        } else {
            return joe.typeName(map) + " {\n" + items.indent(4) + "}";
        }
    }

    //-------------------------------------------------------------------------
    // JoeTest type

    private static class JoeTestType extends ProxyType<Void> {
        JoeTestType() {
            super("JoeTest");

            //**
            // @type JoeTest
            // An API for use by test scripts.
            staticType();

            //**
            // @constant OPAQUE
            // A value of an opaque type, for use in testing.
            constant("OPAQUE", new OpaqueValue());
        }
    }

    private static class OpaqueValue {
        OpaqueValue() {
            // nothing to do
        }
    }
}
