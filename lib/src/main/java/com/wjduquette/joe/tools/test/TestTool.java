package com.wjduquette.joe.tools.test;

import com.wjduquette.joe.*;
import com.wjduquette.joe.app.App;
import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.tools.ToolInfo;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class TestTool implements Tool {
    /**
     * Tool information for this tool, for use by the launcher.
     */
    public static final ToolInfo INFO = new ToolInfo(
        "test",
        "testFile.joe...",
        "Executes Joe tests.",
        """
        Executes a test suite consisting of one or more Joe test scripts,
        accumulating the results.
        
        Tests are defined as no-argument Joe functions with names beginning
        with "test".  They use the various test checkers to check
        results:
        
        - Joe's "assert" statement
        - assertEquals(got, expected)
        - fail(message)
        """,
        TestTool::main
    );

    //-------------------------------------------------------------------------
    // Instance Variables

    private final List<String> testScripts = new ArrayList<>();
    private int loadErrorCount = 0;
    private int successCount = 0;
    private int failureCount = 0;
    private int errorCount = 0;

    //-------------------------------------------------------------------------
    // Constructor

    public TestTool() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // Execution

    public ToolInfo toolInfo() {
        return INFO;
    }

    private void run(String[] args) {
        // FIRST, parse the command line.
        var argq = new ArrayDeque<>(List.of(args));

        if (argq.isEmpty()) {
            printUsage(App.NAME);
            System.exit(1);
        }

        while (!argq.isEmpty()) {
            testScripts.add(argq.poll());
        }

        // NEXT, run the tests.
        for (var path : testScripts) {
            runTest(path);
        }

        // NEXT, print the final results
        var total = successCount + failureCount + errorCount;
        println();
        println("Successes: " + successCount);
        println("Failures:  " + failureCount);
        println("Error:     " + errorCount);
        println("Total:     " + total);

        if (loadErrorCount != 0) {
            println("\n*** " + loadErrorCount + " test file(s) failed to load.");
        }

        if (successCount == total && loadErrorCount == 0) {
            println("\nALL TESTS PASS");
        }
    }

    private void runTest(String scriptPath) {
        System.out.println("\nRunning: " + scriptPath);

        // NEXT, configure the engine.
        var joe = new Joe();
        installTestFunctions(joe);

        // FIRST, load the script.
        try {
            joe.runFile(scriptPath);
        } catch (IOException ex) {
            println("Could not read script: " + scriptPath +
                "\n*** " + ex.getMessage());
            ++loadErrorCount;
        } catch (SyntaxError ex) {
            ex.printErrorsByLine();
            System.out.println(ex.getMessage());
            ++loadErrorCount;
        } catch (JoeError ex) {
            if (ex.line() >= 0) {
                System.err.print("[line " + ex.line() + "] ");
            }
            println(ex.getJoeStackTrace());
            ++loadErrorCount;
        }

        // NEXT, execute its tests.
        var globals = joe.getGlobals();
        var tests = globals.getVarNames().stream()
            .filter(name -> name.startsWith("test"))
            .toList();

        if (tests.isEmpty()) {
            println("  No tests in file.");
            return;
        }

        println();

        for (var test : tests) {
            var value = globals.getVar(test);
            if (value instanceof JoeCallable callable) {
                System.out.printf("%-30s in file %s\n", test, scriptPath);

                try {
                    callable.call(joe, List.of());
                    ++successCount;
                } catch (AssertError ex) {
                    println("  FAILED: " + ex.getMessage());
                    ++failureCount;
                } catch (JoeError ex) {
                    println("  ERROR: " + ex.getMessage());
                    ++errorCount;
                }
            }
        }
    }

    private void installTestFunctions(Joe joe) {
        joe.installGlobalFunction(
            new NativeFunction("assertEquals", this::_assertEquals));
        joe.installGlobalFunction(
            new NativeFunction("fail", this::_fail));

        joe.installScriptResource(getClass(), "lib_test.joe");
    }

    private Object _assertEquals(Joe joe, List<Object> args) {
        Joe.exactArity(args, 2, "assertEquals(got, expected)");
        var got = args.get(0);
        var expected = args.get(1);

        if (!Joe.isEqual(got, expected)) {
            throw new AssertError("Expected '" +
                joe.stringify(expected) + "', got: '" +
                joe.stringify(got) + "'.");
        }

        return null;
    }

    private Object _fail(Joe joe, List<Object> args) {
        Joe.exactArity(args, 1, "fail(message)");
        throw new AssertError(joe.stringify(args.get(0)));
    }

    //-------------------------------------------------------------------------
    // Main

    public static void main(String[] args) {
        new TestTool().run(args);
    }
}
