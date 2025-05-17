package com.wjduquette.joe.tools.nero;

import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.app.App;
import com.wjduquette.joe.nero.OldNero;
import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.tools.ToolInfo;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;

/**
 * The implementation for the {@code joe nero} tool.
 */
public class NeroTool implements Tool {
    /**
     * Tool information for this tool, for use by the launcher.
     */
    public static final ToolInfo INFO = new ToolInfo(
        "nero",
        "[options...] file.nero",
        "Experimental tool for working with Nero scripts.",
        """
        Exercises the Nero script in some way.
        """,
        NeroTool::main
    );

    //-------------------------------------------------------------------------
    // Constructor

    /** Creates the tool. */
    public NeroTool() {
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

        var nero = new OldNero();
        var path = argq.poll();


        try {
            nero.executeFile(path);
        } catch (IOException ex) {
            System.err.println("Could not read script: " + path +
                "\n*** " + ex.getMessage());
            System.exit(1);
        } catch (SyntaxError ex) {
            System.err.println(ex.getErrorReport());
            System.err.println("*** " + ex.getMessage());
            System.exit(65);
        } catch (JoeError ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }


    //-------------------------------------------------------------------------
    // Main

    /**
     * The tool's main routine.
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        new NeroTool().run(args);
    }
}
