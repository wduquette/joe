package com.wjduquette.joe;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static com.wjduquette.joe.checker.Checker.fail;

public class JoeStackTraceTest extends Ted {
    private Joe walker;
    private Joe bert;

    @Before public void setup() {
        this.walker = new Joe(Joe.WALKER);
        this.bert = new Joe(Joe.BERT);
        walker.installGlobalFunction("passThrough", this::_passThrough);
        bert.installGlobalFunction("passThrough", this::_passThrough);
    }


    //-------------------------------------------------------------------------
    // Stack Trace Tests
    //
    // These are here in this test suite rather than in the scripted test suite
    // for three reasons:
    //
    // - It's easier to isolate the normal behavior from the `catch()` behavior.
    // - Script line numbers are stable.
    // - Multiple engines can be tested in one place at the same time, making
    //   reconciliation easier.

    @Test
    public void testStackTrace_scriptedFunctions() {
        test("testStackTrace_scriptedFunctions");
        var script = """
            function a(x) {
                return b(x);
            }
            function b(x) {
                return c(x);
            }
            function c(x) {
                throw "Simulated error!";
            }
            a(0);
            """;
        var trace = """
            Simulated error!
              In function c(x) (*test*:8)
                007 function c(x) {
                008     throw "Simulated error!";
                009 }
              In function b(x) (*test*:5)
              In function a(x) (*test*:2)
              In <script> (*test*:10)
            """;

        dumpScript(script);
        checkRun(walker, script, trace);
        checkRun(bert, script, trace);
    }

    @Test
    public void testStackTrace_lambda() {
        test("testStackTrace_lambda");
        var script = """
            function a(x) {
                return b(x);
            }
            function b(x) {
                return c(\\-> {throw "Simulated Error"; });
            }
            function c(callable) {
                callable();
            }
            a(0);
            """;
        var trace = """
            Simulated Error
              In lambda \\ (*test*:5)
                004 function b(x) {
                005     return c(\\-> {throw "Simulated Error"; });
                006 }
              In function c(callable) (*test*:8)
              In function b(x) (*test*:5)
              In function a(x) (*test*:2)
              In <script> (*test*:10)
            """;
        dumpScript(script);
        checkRun(walker, script, trace);
        checkRun(bert, script, trace);
    }

    @Test
    public void testStackTrace_scriptedMethods() {
        test("testStackTrace_scriptedMethods");
        var script = """
            class Thing {
                method a(x) {
                    return @b(x);
                }
                method b(x) {
                    return @c(x);
                }
                method c(x) {
                    throw "Simulated error!";
                }
            }
            Thing().a(0);
            """;
        var trace = """
            Simulated error!
              In method c(x) (*test*:9)
                008     method c(x) {
                009         throw "Simulated error!";
                010     }
              In method b(x) (*test*:6)
              In method a(x) (*test*:3)
              In <script> (*test*:12)
            """;
        dumpScript(script);
        checkRun(walker, script, trace);
        checkRun(bert, script, trace);
    }

    @Test
    public void testStackTrace_instanceInitializer() {
        test("testStackTrace_instanceInitializer");
        var script = """
            class Thing {
                method init(x) {
                    throw "Simulated error!";
                }
            }
            function make(x) {
                return Thing(x);
            }
            make(0);
            """;
        var trace = """
            Simulated error!
              In initializer init(x) (*test*:3)
                002     method init(x) {
                003         throw "Simulated error!";
                004     }
              In class Thing(x) (*test*:2)
              In function make(x) (*test*:7)
              In <script> (*test*:9)
            """;
        dumpScript(script);
        checkRun(walker, script, trace);
        checkRun(bert, script, trace);
    }

    @Test
    public void testStackTrace_staticMethods() {
        test("testStackTrace_staticMethods");
        var script = """
            class Thing {
                static method a(x) {
                    return Thing.b(x);
                }
                static method b(x) {
                    return Thing.c(x);
                }
                static method c(x) {
                    throw "Simulated error!";
                }
            }
            Thing.a(0);
            """;
        var trace = """
            Simulated error!
              In static method c(x) (*test*:9)
                008     static method c(x) {
                009         throw "Simulated error!";
                010     }
              In static method b(x) (*test*:6)
              In static method a(x) (*test*:3)
              In <script> (*test*:12)
            """;
        dumpScript(script);
        checkRun(walker, script, trace);
        checkRun(bert, script, trace);
    }

    @Test
    public void testStackTrace_staticInitializer() {
        test("testStackTrace_staticInitializer");
        var script = """
            class Thing {
                static {
                    throw "Simulated error!";
                }
            }
            """;
        var trace = """
            Simulated error!
              In static initializer (*test*:3)
                002     static {
                003         throw "Simulated error!";
                004     }
              In class Thing (*test*:2)
              In <script> (*test*:5)
            """;
        dumpScript(script);
        checkRun(walker, script, trace);
        checkRun(bert, script, trace);
    }

    @Test
    public void testStackTrace_nativeFunction() {
        test("testStackTrace_nativeFunction");
        var script = """
            function a(x) {
                return passThrough(x);
            }
            a(false);
            """;
        var trace = """
            Expected callable, got: Boolean 'false'.
              In native function passThrough(...)
              In function a(x) (*test*:2)
                001 function a(x) {
                002     return passThrough(x);
                003 }
              In <script> (*test*:4)
            """;
        dumpScript(script);
        checkRun(walker, script, trace);
        checkRun(bert, script, trace);
    }

    @Test
    public void testStackTrace_nativeMethod() {
        test("testStackTrace_nativeMethod");
        var script = """
            function a(x) {
                return Number.abs(x);
            }
            a(false);
            """;
        var trace = """
            Expected number, got: Boolean 'false'.
              In native static method abs(...)
              In function a(x) (*test*:2)
                001 function a(x) {
                002     return Number.abs(x);
                003 }
              In <script> (*test*:4)
            """;
        dumpScript(script);
        checkRun(walker, script, trace);
        checkRun(bert, script, trace);
    }

    @Test
    public void testStackTrace_nativeInitializer() {
        test("testStackTrace_nativeInitializer");
        var script = """
            function a(x) {
                return Number(x);
            }
            a(false);
            """;
        var trace = """
            Expected numeric string, got: String 'false'.
              In native initializer Number(...)
              In function a(x) (*test*:2)
                001 function a(x) {
                002     return Number(x);
                003 }
              In <script> (*test*:4)
            """;
        dumpScript(script);
        checkRun(walker, script, trace);
        checkRun(bert, script, trace);
    }

    @Test
    public void testStackTrace_nativePassThrough() {
        test("testStackTrace_nativePassThrough");
        walker.installGlobalFunction("passThrough", this::_passThrough);
        var script = """
            function a(x) {
                return passThrough(c);
            }
            function c() {
                throw "Simulated error!";
            }
            a(0);
            """;
        var trace = """
            Simulated error!
              In function c() (*test*:5)
                004 function c() {
                005     throw "Simulated error!";
                006 }
              In java call(<function c()>)
              In native function passThrough(...)
              In function a(x) (*test*:2)
              In <script> (*test*:7)
            """;
        dumpScript(script);
        checkRun(walker, script, trace);
        checkRun(bert, script, trace);
    }

    @Test
    public void testStackTrace_nonCallableInScript() {
        test("testStackTrace_nonCallableInScript");
        var script = """
            function a(x) {
                return b(x);
            }
            function b(x) {
                return x();
            }
            a(0);
            """;
        var trace = """
            Expected callable, got: Number '0'.
              In function b(x) (*test*:5)
                004 function b(x) {
                005     return x();
                006 }
              In function a(x) (*test*:2)
              In <script> (*test*:7)
            """;
        dumpScript(script);
        checkRun(walker, script, trace);
        checkRun(bert, script, trace);
    }

    private Object _passThrough(Joe joe, Args args) {
        args.exactArity(1, "passThrough(callable)");
        var callee = args.next();
        if (!joe.isCallable(callee)) {
            throw joe.expected("callable", callee);
        }
        return joe.call(callee);
    }

    private void checkRun(Joe joe, String script, String expected) {
        var engine = (joe == bert) ? "Bert" : "Walker";
        var got = "";
        try {
            joe.run("*test*", script);
            fail("traceOf expects to throw an error.");
            return; // Make the compiler happy.
        } catch (JoeError ex) {
            got = ex.getJoeStackTrace();
        }

        if (got.equals(expected)) {
            return;
        }

        System.out.println(toColumns(
            "Expected Trace:\n" + expected,
            "From " + engine + " engine:\n" + got));
        fail("Stack traces do not match!");
    }

    private String runTrace(Joe joe, String script) {
        try {
            joe.run("*test*", script);
            fail("traceOf expects to throw an error.");
            return ""; // Make the compiler happy.
        } catch (JoeError ex) {
            return ex.getJoeStackTrace();
        }
    }

    private String toColumns(String a, String b) {
        var aLines = a.lines().toList();
        var bLines = b.lines().toList();
        int aMax = aLines.stream()
            .mapToInt(String::length)
            .max().orElse(0);

        var n = Math.max(aLines.size(), bLines.size());
        var result = new ArrayList<String>();
        for (var i = 0; i < n; i++) {
            var aLine = i < aLines.size() ? aLines.get(i) : "";
            var bLine = i < bLines.size() ? bLines.get(i) : "";
            var line = pad(aLine, aMax) + "  " + bLine;
            result.add(line);
        }

        return String.join("\n", result);
    }

    private String pad(String s, int width) {
        return (s + " ".repeat(width)).substring(0, width);
    }

    private void dumpScript(String script) {
        var lines = script.lines().toList();

        for (var i = 1; i <= lines.size(); i++) {
            System.out.printf("%02d %s\n", i, lines.get(i - 1));
        }
        System.out.println();
    }
}
