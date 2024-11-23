package com.wjduquette.joe.tools;

import com.wjduquette.joe.app.App;
import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.console.ConsolePackage;

import java.io.IOException;
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
        "file.joe",
        "Executes a Joe script.",
        """
        Executes the script.
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

        var joe = new Joe();
        var path = argq.poll();

        var consolePackage = new ConsolePackage();
        consolePackage.setScript(path);
        consolePackage.getArgs().addAll(argq);
        joe.installPackage(consolePackage);

        try {
            joe.runFile(path);
        } catch (IOException ex) {
            System.err.println("Could not read script: " + path +
                "\n*** " + ex.getMessage());
            System.exit(1);
        } catch (SyntaxError ex) {
            ex.printDetails();
            System.err.println(ex.getMessage());
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
