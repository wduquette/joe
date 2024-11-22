package com.wjduquette.joe.tools.test;

import com.wjduquette.joe.*;
import com.wjduquette.joe.app.App;
import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.tools.ToolInfo;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the `joe test` tool.
 */
public class TestTool implements Tool {
    /**
     * Tool information for this tool, for use by the launcher.
     */
    public static final ToolInfo INFO = new ToolInfo(
        "test",
        "options... testFile.joe...",
        "Executes Joe tests.",
        """
        Executes a test suite consisting of one or more Joe test scripts,
        accumulating the results.
        
        Options:
        
        --verbose, -v
            Enable verbose output.  Normally only failures and the final
            summary are included in the output.  If this is given, all test
            output is included.
        
        Test Scripts
        
        A test script is a Joe script containing one or more no-argument Joe
        functions with names beginning with "test".  A test fails if it
        throws a Joe AssertError, either via Joe's assert statement or by
        using one of the test checkers defined by the test tool.  See
        the Joe User's Guide for more details.
        """,
        TestTool::main
    );

    //-------------------------------------------------------------------------
    // Instance Variables

    boolean verbose = false;
    private final List<String> testScripts = new ArrayList<>();
    private int loadErrorCount = 0;
    private int successCount = 0;
    private int skipCount = 0;
    private int failureCount = 0;
    private int errorCount = 0;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the tool.
     */
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
            var arg = argq.poll();

            switch (arg) {
                case "-v", "--verbose" -> verbose = true;
                default -> testScripts.add(arg);
            }
        }

        // NEXT, run the tests.
        for (var path : testScripts) {
            runTest(path);
        }

        // NEXT, print the final results
        var total = successCount + skipCount + failureCount + errorCount;
        println();
        System.out.printf("Successes  %5d\n", successCount);
        System.out.printf("Skipped    %5d\n", skipCount);
        System.out.printf("Failures   %5d\n", failureCount);
        System.out.printf("Errors     %5d\n", errorCount);
        System.out.println("---------- -----");
        System.out.printf("Total      %5d\n", total);

        if (loadErrorCount != 0) {
            println("\n*** " + loadErrorCount + " test file(s) failed to load.");
        }

        if (successCount == total && loadErrorCount == 0) {
            println("\nALL TESTS PASS");
        }
    }

    private void runTest(String scriptPath) {
        if (verbose) {
            println("\nRunning: " + scriptPath);
        }

        // NEXT, configure the engine.
        var joe = new Joe();
        joe.installPackage(TestPackage.PACKAGE);

        // FIRST, load the script.
        try {
            joe.runFile(scriptPath);
        } catch (IOException ex) {
            println("Could not read script: " + scriptPath +
                "\n*** " + ex.getMessage());
            ++loadErrorCount;
        } catch (SyntaxError ex) {
            ex.printDetails();
            System.out.println(ex.getMessage());
            ++loadErrorCount;
        } catch (JoeError ex) {
            System.out.print("*** Error in script: ");
            println(ex.getJoeStackTrace());
            ++loadErrorCount;
        }

        // NEXT, execute its tests.
        var tests = joe.getVarNames().stream()
            .filter(name -> name.startsWith("test"))
            .toList();

        if (tests.isEmpty()) {
            println("***  No tests in: " + scriptPath);
            return;
        }

        if (verbose) {
            println();
            runVerbosely(joe, tests);
        } else {
            runQuietly(joe, scriptPath, tests);
        }
    }

    private void runVerbosely(Joe joe, List<String> tests) {
        for (var test : tests) {
            var value = joe.getVar(test);
            if (!(value instanceof JoeCallable callable)) {
                continue;
            }

            println("+++ " + test);

            try {
                joe.call(callable);
                ++successCount;
            } catch (SkipError ex) {
                println("  SKIPPED: " + ex.getMessage());
                ++skipCount;
            } catch (AssertError ex) {
                println("  FAILED: " + ex.getJoeStackTrace());
                ++failureCount;
            } catch (JoeError ex) {
                println("  ERROR: " + ex.getJoeStackTrace());
                ++errorCount;
            }
        }
    }

    private void runQuietly(Joe joe, String scriptPath, List<String> tests) {
        for (var test : tests) {
            var value = joe.getVar(test);
            if (!(value instanceof JoeCallable callable)) {
                continue;
            }

            String result;
            Exception error;

            try {
                joe.call(callable);
                ++successCount;
                result = null;
                error = null;
            } catch (SkipError ex) {
                ++skipCount;
                result = "SKIPPED";
                error = ex;
            } catch (AssertError ex) {
                ++failureCount;
                result = "FAILED";
                error = ex;
            } catch (JoeError ex) {
                ++errorCount;
                result = "ERROR";
                error = ex;
            }

            if (error != null) {
                System.out.printf("%-8s %s %s, %s\n",
                    result + ":", scriptPath, test, error.getMessage());
            }
        }
    }

    //-------------------------------------------------------------------------
    // SkipException

    // Used by the "skip()" function.
    public static class SkipError extends JoeError {
        public SkipError(String message) { super(message); }
    }

    //-------------------------------------------------------------------------
    // Main

    /**
     * The tool's main routine.
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        new TestTool().run(args);
    }
}
