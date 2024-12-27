package com.wjduquette.joe.app;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.tools.ToolInfo;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;

/**
 * The implementation for the {@code joe dump} tool.
 */
public class DumpTool implements Tool {
    /**
     * Tool information for this tool, for use by the launcher.
     */
    public static final ToolInfo INFO = new ToolInfo(
        "dump",
        "[options...] file.joe",
        "Compiles the Joe script and dumps the compiled form.",
        """
        Dumps compilation details for the script.  The options
        are as follows:
        
        --bert, -b     Use the experimental "Bert" byte-engine.
        """,
        DumpTool::main
    );

    //-------------------------------------------------------------------------
    // Constructor

    /** Creates the tool. */
    public DumpTool() {
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

        var engineType = Joe.WALKER;

        while (!argq.isEmpty() && argq.peek().startsWith("-")) {
            var opt = argq.poll();
            switch (opt) {
                case "--bert", "-b" -> engineType = Joe.BERT;
                default -> {
                    System.err.println("Unknown option: '" + opt + "'.");
                    System.exit(64);
                }
            }
        }

        var joe = new Joe(engineType);
        var path = argq.poll();

        try {
            System.out.println(joe.dumpFile(path));
        } catch (IOException ex) {
            System.err.println("Could not read script: " + path +
                "\n*** " + ex.getMessage());
            System.exit(1);
        } catch (SyntaxError ex) {
            System.err.println(ex.getErrorReport());
            System.err.println("*** " + ex.getMessage());
            System.exit(65);
        }
    }


    //-------------------------------------------------------------------------
    // Main

    /**
     * The tool's main routine.
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        new DumpTool().run(args);
    }
}
