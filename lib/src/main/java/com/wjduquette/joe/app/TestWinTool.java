package com.wjduquette.joe.app;

import com.wjduquette.joe.*;
import com.wjduquette.joe.runner.TestRunner;
import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.tools.ToolInfo;
import com.wjduquette.joe.win.WinPackage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the `joe test` tool.
 */
public class TestWinTool implements Tool {
    /**
     * Tool information for this tool, for use by the launcher.
     */
    public static final ToolInfo INFO = ToolInfo.define()
        .name("testwin")
        .argsig("options... testFile.joe...")
        .oneLiner("Executes Joe tests in the JavaFX context.")
        .javafx(true)
        .launcher(TestWinTool::main)
        .help("""
        Executes a test suite consisting of one or more Joe test scripts
        in the JavaFX context, accumulating the results.
        
        Options:
        
        --test name, -t name
            Executes tests whose name contains the given value
        --verbose, -v
            Enable verbose output.
        --libpath path, --l path
           Sets the library path to the given path.
        --clark, -c
            Use the "Clark" byte-engine (default)
        --walker, -w
            Use the "Walker" AST-walker engine.
        
        Test Scripts
        
        A test script is a Joe script containing one or more no-argument Joe
        functions with names beginning with "test".  A test fails if it
        throws a Joe AssertError, either via Joe's assert statement or by
        using one of the test checkers defined by the test tool.  See
        the Joe User's Guide for more details.
        
        Testing Joe Libraries
        
        If the --libpath option is used, the tool searches for
        Joe packages on the given library path, which must be a colon-delimited
        list of folder paths.
        
        NOTE: `joe test` does not load packages from the JOE_LIB_PATH, as it
        assumes that the user is testing the library from within a code
        repository, not as installed for general use.
        """)
        .build();

    //-------------------------------------------------------------------------
    // Instance Variables

    private String engineType = Joe.CLARK;
    private String testName = null;
    private String libPath = null;
    private boolean verbose = false;
    private final List<String> testScripts = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the tool.
     */
    public TestWinTool() {
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
                case "--test", "-t" -> testName = toOptArg(arg, argq);
                case "--libpath", "-l" -> libPath = toOptArg(arg, argq);
                case "--clark", "-c" -> engineType = Joe.CLARK;
                case "--walker", "-w" -> engineType = Joe.WALKER;
                case "-v", "--verbose" -> verbose = true;
                default -> testScripts.add(arg);
            }
        }

        // NEXT, run the tests.  Note: we are already on the JavaFX thread.
        var runner = TestRunner.define()
            .appName("joe test " + App.getVersion())
            .engineType(engineType)
            .install(WinPackage.PACKAGE)
            .libPath(libPath != null ? libPath : System.getenv(Joe.JOE_LIB_PATH))
            .testScripts(testScripts)
            .verbose(verbose)
            .testName(testName)
            .build();
        runner.run();

        // NEXT, be sure we exit, even if there's a window hanging around.
        System.exit(0);
    }

    //-------------------------------------------------------------------------
    // Main

    /**
     * The tool's main routine.
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        new TestWinTool().run(args);
    }
}
