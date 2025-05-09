package com.wjduquette.joe.app;

import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.console.ConsolePackage;
import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.tools.ToolInfo;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;

/**
 * The implementation for the {@code joe run} tool.
 */
public class RunTool implements Tool {
    /**
     * Tool information for this tool, for use by the launcher.
     */
    public static final ToolInfo INFO = new ToolInfo(
        "run",
        "[options...] file.joe",
        "Executes a Joe script.",
        """
        Executes the script.  The options are as follows:
        
        --clark,  -c   Use the "Clark" byte-engine (default)
        --walker, -w   Use the "Walker" AST-walker engine.
        --bert,   -b   Use the "Bert" byte-engine (legacy).
        --debug,  -d   Enable debugging output.  This is mostly of use to
                       the Joe maintainer.
        """,
        RunTool::main
    );

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
        var debug = false;
        var measureRuntime = false;

        while (!argq.isEmpty() && argq.peek().startsWith("-")) {
            var opt = argq.poll();
            switch (opt) {
                case "--bert", "-b" -> engineType = Joe.BERT;
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

        var joe = new Joe(engineType);
        joe.setDebug(debug);
        var path = argq.poll();

        var consolePackage = new ConsolePackage();
        consolePackage.setScript(path);
        consolePackage.getArgs().addAll(argq);
        joe.installPackage(consolePackage);

        try {
            if (debug) {
                System.out.println("Joe " + App.getVersion() + " (" +
                    joe.engineName() + " engine)");
            }
            var startTime = Instant.now();
            joe.runFile(path);
            var endTime = Instant.now();

            if (measureRuntime) {
                var duration = Duration.between(startTime, endTime).toMillis() / 1000.0;
                System.out.printf("Run-time: %.3f seconds\n", duration);
            }
        } catch (IOException ex) {
            System.err.println("Could not read script: " + path +
                "\n*** " + ex.getMessage());
            System.exit(1);
        } catch (SyntaxError ex) {
            System.err.println(ex.getErrorReport());
            System.err.println("*** " + ex.getMessage());
            System.exit(65);
        } catch (JoeError ex) {
            System.err.print("*** Error in script: ");
            System.err.println(ex.getJoeStackTrace());
            System.exit(70);
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
