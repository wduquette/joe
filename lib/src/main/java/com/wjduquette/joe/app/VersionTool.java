package com.wjduquette.joe.app;

import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.tools.ToolInfo;

import java.util.ArrayDeque;
import java.util.List;

/**
 * The implementation for the {@code joe version} tool.
 */
public class VersionTool implements Tool {
    /**
     * Tool information for this tool, for use by the launcher.
     */
    public static final ToolInfo INFO = new ToolInfo(
        "version",
        "",
        "Prints Joe's version and build information.",
        """
        Prints Joe's version and build information.
        """,
        VersionTool::main
    );

    //-------------------------------------------------------------------------
    // Constructor

    /** Creates the tool. */
    public VersionTool() {
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

        if (!argq.isEmpty()) {
            printUsage(App.NAME);
            System.exit(64);
        }

        var attrs = App.getManifestAttributes();
        if (attrs != null) {
            var version = attrs.getValue("Implementation-Version");
            var date = attrs.getValue("Built-Date");

            if (version != null) {
                println("Joe " + version + ", built " + date);
            } else {
                println("Joe (dev build)");
            }
        } else {
            println("Joe (dev build)");
        }
    }

    //-------------------------------------------------------------------------
    // Main

    /**
     * The tool's main routine.
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        new VersionTool().run(args);
    }
}
