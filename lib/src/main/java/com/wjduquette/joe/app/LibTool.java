package com.wjduquette.joe.app;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.PackageFinder;
import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.tools.ToolInfo;

import java.util.ArrayDeque;
import java.util.List;

/**
 * The implementation for the {@code joe lib} tool.
 */
public class LibTool implements Tool {
    /**
     * Tool information for this tool, for use by the launcher.
     */
    public static final ToolInfo INFO = new ToolInfo(
        "lib",
        "[options...]",
        "Searches for local Joe packages.",
        """
        Searches the local disk for Joe packages.  By default it checks
        the folders on the JOE_LIB_PATH, a colon-delimited list of
        library folders.  Lists all found packages, along with any
        errors encountered while searching.
        
        Options:
        
        --path path     Search the given path instead of JOE_LIB_PATH.
        --check         Verifies that all packages found can be loaded.
        
        See the Joe User's Guide for information on how to create a
        local package.
        """,
        LibTool::main
    );

    //-------------------------------------------------------------------------
    // Constructor

    /** Creates the tool. */
    public LibTool() {
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
        var doCheck = false;
        var libPath = System.getenv(Joe.JOE_LIB_PATH);

        while (!argq.isEmpty() && argq.peek().startsWith("-")) {
            var opt = argq.poll();
            switch (opt) {
                case "--path" -> libPath = toOptArg(opt, argq);
                case "--check" -> doCheck = true;
                default -> {
                    System.err.println("Unknown option: '" + opt + "'.");
                    System.exit(64);
                }
            }
        }

        if (libPath == null || libPath.isEmpty()) {
            System.err.println(
                "No library path to search; use --path or set JOE_LIB_PATH.");
            System.exit(64);
        }

        var finder = new PackageFinder();
        finder.findPackages(libPath, true);

        if (doCheck) {
            System.out.println("--check is not yet implemented.");
        }
    }


    //-------------------------------------------------------------------------
    // Main

    /**
     * The tool's main routine.
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        new LibTool().run(args);
    }
}
