package com.wjduquette.joe.tools;

import com.wjduquette.joe.app.App;
import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;

public class RunTool implements Tool {
    /**
     * Tool information for this tool, for use by the launcher.
     */
    public static final ToolInfo INFO = new ToolInfo(
        "run",
        "file.joe",
        "Executes a Joe script.",
        """
        Executes the script.
        """,
        RunTool::main
    );

    //-------------------------------------------------------------------------
    // Constructor

    public RunTool() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // Execution

    public ToolInfo toolInfo() {
        return INFO;
    }

    private void run(String[] args) {
        var argq = new ArrayDeque<>(List.of(args));

        if (argq.size() != 1) {
            printUsage(App.NAME);
            System.exit(64);
        }

        var joe = new Joe();
        var path = argq.poll();

        try {
            joe.runFile(path);
        } catch (IOException ex) {
            System.err.println("Could not read script: " + path +
                "\n*** " + ex.getMessage());
            System.exit(1);
        } catch (SyntaxError ex) {
            ex.printErrorsByLine();
            System.err.println(ex.getMessage());
            System.exit(65);
        } catch (JoeError ex) {
            if (ex.line() >= 0) {
                System.err.print("[line " + ex.line() + "] ");
            }
            System.err.println(ex.getJoeStackTrace());
            System.exit(70);
        }
    }


    //-------------------------------------------------------------------------
    // Main

    public static void main(String[] args) {
        new RunTool().run(args);
    }
}
