package com.wjduquette.joe.app;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.runner.JoeRunner;
import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.tools.ToolInfo;

import java.util.ArrayDeque;
import java.util.List;

/**
 * The implementation for the {@code joe run} tool.
 */
public class RunTool implements Tool {
    /**
     * Tool information for this tool, for use by the launcher.
     */
    public static final ToolInfo INFO = ToolInfo.define()
        .name("run")
        .argsig("[options...] file.joe")
        .oneLiner("Executes a Joe script.")
        .help("""
        Executes the script.
        
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
        .launcher(RunTool::main)
        .build();

    //-------------------------------------------------------------------------
    // Constructor

    /** Creates the tool. */
    public RunTool() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // Execution

    /**
     * Gets implementation info about the tool.
     * @return The info.
     */
    public ToolInfo toolInfo() {
        return INFO;
    }

    private void run(String[] args) {
        var argq = new ArrayDeque<>(List.of(args));

        if (argq.isEmpty()) {
            printUsage(App.NAME);
            System.exit(64);
        }

        var engineType = Joe.CLARK;
        String libPath = null;
        var debug = false;
        var measureRuntime = false;

        while (!argq.isEmpty() && argq.peek().startsWith("-")) {
            var opt = argq.poll();
            switch (opt) {
                case "--libpath", "-l" -> libPath = toOptArg(opt, argq);
                case "--clark", "-c" -> engineType = Joe.CLARK;
                case "--walker", "-w" -> engineType = Joe.WALKER;
                case "--time", "-t" -> measureRuntime = true;
                case "--debug", "-d" -> debug = true;
                default -> {
                    System.err.println("Unknown option: '" + opt + "'.");
                    System.exit(64);
                }
            }
        }

        var runner = JoeRunner.define()
            .appName("Joe " + App.getVersion())
            .engineType(engineType)
            .debug(debug)
            .scriptPath(argq.poll())
            .scriptArgs(argq)
            .libPath(libPath != null ? libPath : System.getenv(Joe.JOE_LIB_PATH))
            .build();
        runner.execute();

        if (measureRuntime) {
            var runTime = runner.getRunTime().toMillis() / 1000.0;
            System.out.printf("Run-time: %.3f seconds\n", runTime);
        }
    }


    //-------------------------------------------------------------------------
    // Main

    /**
     * The tool's main routine.
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        new RunTool().run(args);
    }
}
