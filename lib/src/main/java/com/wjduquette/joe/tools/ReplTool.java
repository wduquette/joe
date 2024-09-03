package com.wjduquette.joe.tools;

import com.wjduquette.joe.app.App;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.SyntaxError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    // Instance Variables

    private final Joe joe;

    //-------------------------------------------------------------------------
    // Constructor

    public ReplTool() {
        this.joe = new Joe();
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

        try {
            runPrompt();
        } catch (IOException ex) {
            System.err.print(
                "I/O Error while reading from console: " +
                ex.getMessage());
        }
    }

    public void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            try {
                var result = joe.run(line);

                if (result != null) {
                    System.out.println("-> " + joe.stringify(result));
                }
            } catch (SyntaxError ex) {
                ex.printErrorsByLine();
            } catch (JoeError ex) {
                System.err.println("*** " + ex.getJoeStackTrace());
            }
        }
    }



    //-------------------------------------------------------------------------
    // Main

    public static void main(String[] args) {
        new ReplTool().run(args);
    }
}
