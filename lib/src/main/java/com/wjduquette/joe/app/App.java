package com.wjduquette.joe.app;

import com.wjduquette.joe.tools.ReplTool;
import com.wjduquette.joe.tools.RunTool;
import com.wjduquette.joe.tools.VersionTool;
import com.wjduquette.joe.tools.doc.DocTool;
import com.wjduquette.joe.tools.win.WinTool;
import com.wjduquette.joe.tools.test.TestTool;
import com.wjduquette.joe.tools.ToolLauncher;

import java.util.List;

/**
 * The top-level class for Joe's command-line tool.
 */
public class App {
    private App() {} // Not instantiable

    /**
     * The expected name of Joe's command-lien app.
     */
    public static final String NAME = "joe";

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
            RunTool.INFO,
            ReplTool.INFO,
            TestTool.INFO,
            DocTool.INFO,
            WinTool.INFO
        ));

        try {
            launcher.launch(args);
        } catch (Exception ex) {
            System.err.println("Unexpected exception: " + ex);
            ex.printStackTrace(System.err);
        }
    }
}
