package com.wjduquette.joe;

import com.wjduquette.joe.tools.ReplTool;
import com.wjduquette.joe.tools.RunTool;
import com.wjduquette.joe.tools.ToolLauncher;

import java.io.IOException;
import java.util.List;

public class App {
    public static final String NAME = "joe";

    //-------------------------------------------------------------------------
    // Main

    public static void main(String[] args) {
        var launcher = new ToolLauncher(NAME, List.of(
            RunTool.INFO,
            ReplTool.INFO
        ));

        try {
            launcher.launch(args);
        } catch (Exception ex) {
            System.err.println("Unexpected exception: " + ex);
            ex.printStackTrace(System.err);
        }
    }
}
