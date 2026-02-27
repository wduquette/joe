package com.wjduquette.joe.app;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.runner.JoeWinRunner;
import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.tools.ToolInfo;

import java.util.ArrayDeque;
import java.util.List;

/**
 * A tool that runs a Joe script with the JavaFX WinPackage.
 */
public class WinTool implements Tool {
    /** The tool's metadata. */
    public static final ToolInfo INFO = ToolInfo.define()
        .name("win")
        .argsig("script.joe args...")
        .oneLiner("Displays a scripted GUI.")
        .javafx(true)
        .launcher(WinTool::main)
        .help("""
        Given a script, this tool displays a JavaFX GUI.  The tool provides
        the Joe standard library along with the optional joe.console and
        joe.win packages.  See the Joe User's Guide for details.
        
        The tool searches for locally installed Joe packages on a user-provided
        library path, which must be a colon-delimited list of folder paths.
        By default, the library path is provided by the JOE_LIB_PATH environment
        variable (if defined); otherwise, the user can specify a library path
        via the --libpath option.
        
        The options are as follows:
        
        --libpath path, --l path
           Sets the library path to the given path.
        --clark, -c
            Use the "Clark" byte-engine (default)
        --walker, -w
            Use the "Walker" AST-walker engine.
        --debug, -d
            Enable debugging output.  This is mostly of use to
            the Joe maintainer.
        """)
        .build();

    //------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the tool.
     */
    public WinTool() {
        // Nothing to do
    }

    //------------------------------------------------------------------------
    // Execution

    /**
     * Gets implementation info about the tool.
     * @return The info.
     */
    public ToolInfo toolInfo() {
        return INFO;
    }


    /**
     * Runs the tool given the command-line arguments.
     * @param args The arguments.
     */
    public void run(String[] args) {
        // FIRST, prepare to handle uncaught exceptions in the background.
        Thread.currentThread().setUncaughtExceptionHandler(
            (thread, ex) -> handleUncaughtException(false, ex));

        // NEXT, parse the command line arguments.
        var argq = new ArrayDeque<>(List.of(args));
        if (argq.isEmpty()) {
            printUsage(App.NAME);
            exit(64);
        }

        // NEXT, parse the options.
        var engineType = Joe.CLARK;
        String libPath = null;
        var debug = false;

        while (!argq.isEmpty() && argq.peek().startsWith("-")) {
            var opt = argq.poll();
            switch (opt) {
                case "--libpath", "-l" -> libPath = toOptArg(opt, argq);
                case "--clark", "-c" -> engineType = Joe.CLARK;
                case "--walker", "-w" -> engineType = Joe.WALKER;
                case "--debug", "-d" -> debug = true;
                default -> {
                    System.err.println("Unknown option: '" + opt + "'.");
                    System.exit(64);
                }
            }
        }

        var runner = JoeWinRunner.define()
            .appName("joe win " + App.getVersion())
            .engineType(engineType)
            .debug(debug)
            .scriptPath(argq.poll())
            .scriptArgs(argq)
            .libPath(libPath != null ? libPath : System.getenv(Joe.JOE_LIB_PATH))
            .build();
        runner.execute();
    }

    //------------------------------------------------------------------------
    // Main

    /**
     * Launches the tool
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        var tool = new WinTool();
        tool.run(args);
    }
}
