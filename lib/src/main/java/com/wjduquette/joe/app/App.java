package com.wjduquette.joe.app;

import com.wjduquette.joe.tools.doc.DocTool;
import com.wjduquette.joe.tools.test.TestWinTool;
import com.wjduquette.joe.tools.win.WinTool;
import com.wjduquette.joe.tools.test.TestTool;
import com.wjduquette.joe.tools.ToolLauncher;

import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * The top-level class for Joe's command-line tool.
 */
public class App {
    private App() {} // Not instantiable

    /**
     * The expected name of Joe's command-line app.
     */
    public static final String NAME = "joe";

    /**
     * Gets Joe's version number, or "?.?.?" if unavailable.
     * @return The version number.
     */
    public static String getVersion() {
        var attrs = getManifestAttributes();
        if (attrs != null) {
            var version = attrs.getValue("Implementation-Version");
            if (version != null) {
                return version;
            }
        }
        return "?.?.?";
    }

    /**
     * Get the manifest attributes, or return null on failure.
     * @return The attributes, or null.
     */
    public static Attributes getManifestAttributes() {
        try (var stream = App.class.getResourceAsStream("/META-INF/MANIFEST.MF")) {
            return new Manifest(stream).getMainAttributes();
        } catch (Exception ex) {
            return null;
        }
    }

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
            LibTool.INFO,
            DumpTool.INFO,
            TestTool.INFO,
            TestWinTool.INFO,
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
