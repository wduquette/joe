package com.wjduquette.joe.app;

import com.wjduquette.joe.tools.ToolLauncher;

import java.util.List;

/**
 * The top-level class for Nero's command-line app.
 */
public class NeroApp {
    private NeroApp() {} // Not instantiable

    /**
     * The expected name of Nero's command-line app.
     */
    public static final String NAME = "nero";

    //-------------------------------------------------------------------------
    // Main

    /**
     * The application's main routine.  The first argument is the name of
     * the tool to execute; the remainder are passed to the tool.
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        var launcher = new ToolLauncher(NAME, List.of(
            VersionTool.INFO,
            NeroRunTool.INFO
        ));

        try {
            launcher.launch(args);
        } catch (Exception ex) {
            System.err.println("Unexpected exception: " + ex);
            ex.printStackTrace(System.err);
        }
    }
}
