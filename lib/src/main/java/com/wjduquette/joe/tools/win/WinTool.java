package com.wjduquette.joe.tools.win;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.app.App;
import com.wjduquette.joe.console.ConsolePackage;
import com.wjduquette.joe.win.WinPackage;
import com.wjduquette.joe.tools.FXTool;
import com.wjduquette.joe.tools.ToolInfo;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Deque;

/**
 * A tool that runs a Joe script with the JavaFX WinPackage.
 */
public class WinTool extends FXTool {
    /** The tool's metadata. */
    public static final ToolInfo INFO = new ToolInfo(
        "win",
        "script.joe args...",
        "Displays a scripted GUI.",
        """
        Given a script, this tool displays a JavaFX GUI.  The tool provides
        the Joe standard library along with the optional joe.console and
        joe.win packages.  See the Joe User's Guide for details.
        
        Executes the script.  The options are as follows:
        
        --bert,   -b   Use the "Bert" byte-engine (default).
        --walker, -w   Use the "Walker" AST-walker engine.
        --debug,  -d   Enable debugging output.  This is mostly of use to
                       the Joe maintainer.
        """,
        WinTool::main
    );

    //------------------------------------------------------------------------
    // Instance Variables

    // Keeps joe from being collected.
    private Joe joe;
    private final VBox root = new VBox();

    //------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the tool.
     */
    public WinTool() {
        super(INFO);
    }

    //------------------------------------------------------------------------
    // Main-line code

    @Override
    public void run(Stage stage, Deque<String> args) {
        // FIRST, parse the command line arguments.
        if (args.isEmpty()) {
            printUsage(App.NAME);
            exit(64);
        }

        // NEXT, parse the options.
        var engineType = Joe.BERT;
        var debug = false;

        while (!args.isEmpty() && args.peek().startsWith("-")) {
            var opt = args.poll();
            switch (opt) {
                case "--bert", "-b" -> engineType = Joe.BERT;
                case "--walker", "-w" -> engineType = Joe.WALKER;
                case "--debug", "-d" -> debug = true;
                default -> {
                    System.err.println("Unknown option: '" + opt + "'.");
                    System.exit(64);
                }
            }
        }

        var joe = new Joe(engineType);
        joe.setDebug(debug);

        if (debug) {
            System.out.println("Joe " + App.getVersion() + " (" +
                joe.engineName() + " engine)");
        }

        // NEXT, create the scene with default settings.
        Scene scene = new Scene(root, 400, 300);
//        scene.getStylesheets().add("file:foo.css");
        stage.setTitle("joe win");
        stage.setScene(scene);

        // NEXT, load the required packages
        var path = args.poll();
        var consolePackage = new ConsolePackage();
        consolePackage.setScript(path);
        consolePackage.getArgs().addAll(args);
        joe.installPackage(consolePackage);

        var guiPackage = new WinPackage(stage, root);
        joe.installPackage(guiPackage);

        // NEXT, execute the script.
        try {
            joe.runFile(path);
        } catch (IOException ex) {
            System.err.println("Could not read script: " + path +
                "\n*** " + ex.getMessage());
            System.exit(1);
        } catch (SyntaxError ex) {
            System.err.println(ex.getErrorReport());
            System.err.println(ex.getMessage());
            System.exit(65);
        } catch (JoeError ex) {
            System.err.print("*** Error in script: ");
            System.err.println(ex.getJoeStackTrace());
            System.exit(70);
        }

        // NEXT, pop up the window
        stage.show();
    }

    //------------------------------------------------------------------------
    // Main

    /**
     * Launches the tool
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
