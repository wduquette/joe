package com.wjduquette.joe.tools;

import com.wjduquette.joe.App;
import com.wjduquette.joe.Joe;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;

public class ReplTool implements Tool {
    /**
     * Tool information for this tool, for use by the launcher.
     */
    public static final ToolInfo INFO = new ToolInfo(
        "repl",
        "",
        "Invokes a simple Joe REPL.",
        """
        Invokes the REPL.  
        
        - To exit, press ^D.
        - To execute a statement, enter it as it would appear in a
          script.
        - To evaluate an expression, terminate it with a semicolon:
        
            > 1 + 1;
            -> 2
            > 
        
        NOTE: At present, the REPL doesn't offer history, multi-line
        input, etc.
        """,
        ReplTool::main
    );

    //-------------------------------------------------------------------------
    // Constructor

    public ReplTool() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // Execution

    public ToolInfo toolInfo() {
        return INFO;
    }

    private void run(String[] args) {
        var argq = new ArrayDeque<>(List.of(args));

        if (!argq.isEmpty()) {
            printUsage(App.NAME);
            System.exit(64);
        }

        var joe = new Joe();
        try {
            joe.runPrompt();
        } catch (IOException ex) {
            System.err.print(
                "I/O Error while reading from console: " +
                ex.getMessage());
        }
    }


    //-------------------------------------------------------------------------
    // Main

    public static void main(String[] args) {
        new ReplTool().run(args);
    }
}
