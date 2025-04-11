package com.wjduquette.joe.clark;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.Ted;
import com.wjduquette.joe.UnexpectedError;
import org.junit.Before;
import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.check;
import static com.wjduquette.joe.checker.Checker.fail;

public class ClarkEngineTest extends Ted {
    private Joe joe;

    @Before public void setup() {
        this.joe = new Joe(Joe.CLARK);
        joe.installGlobalFunction("unexpected", this::_unexpected);
    }

    @Test
    public void testUnexpectedException_run() {
        test("testUnexpectedException_run");
        try {
            joe.run("*test*", """
                unexpected("simulated");
            """);
            fail("Expected error.");
        } catch (UnexpectedError ex) {
            println("Got error: " + ex.getMessage());
            check(ex.getMessage())
                .eq("Unexpected Java error: java.lang.UnsupportedOperationException: simulated");
            check(ex.getCause() instanceof UnsupportedOperationException)
                .eq(true);
        }
    }

    @Test
    public void testUnexpectedException_call() {
        test("testUnexpectedException_run");
        try {
            var unexpected = joe.getVar("unexpected");
            joe.call(unexpected, "simulated");
            fail("Expected error.");
        } catch (UnexpectedError ex) {
            println("Got error: " + ex.getMessage());
            check(ex.getMessage())
                .eq("Unexpected Java error: java.lang.UnsupportedOperationException: simulated");
            check(ex.getCause() instanceof UnsupportedOperationException)
                .eq(true);
        }
    }

    @Test
    public void testCallClass() {
        test("testCallClass");

        // Verify that the client can invoke a class by name to create an
        // instance.
        var script = """
            class Thing {
                method init(name) { @name = name; }
            }
            """;

        // Test with Walker
        joe.run("*test*", script);
        var cls = joe.getVar("Thing");

        var result = joe.call(cls, "fred");
        check(result instanceof ClarkInstance).eq(true);

        if (result instanceof ClarkInstance thing) {
            check(thing.get("name")).eq("fred");
        }
    }

    //-------------------------------------------------------------------------
    // Helpers

    private Object _unexpected(Joe joe, Args args) {
        args.exactArity(1, "unexpected(message)");
        throw new UnsupportedOperationException(joe.stringify(args.next()));
    }
}
