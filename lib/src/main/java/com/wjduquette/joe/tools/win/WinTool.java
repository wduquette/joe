package com.wjduquette.joe.tools.win;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.PackageFinder;
import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.app.App;
import com.wjduquette.joe.console.ConsolePackage;
import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.win.WinPackage;
import com.wjduquette.joe.tools.ToolInfo;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;

/**
 * A tool that runs a Joe script with the JavaFX WinPackage.
 */
public class WinTool implements Tool {
    /** The tool's metadata. */
    public static final ToolInfo INFO = ToolInfo.define()
        .name("win")
        .argsig("script.joe args...")
        .oneLiner("Displays a scripted GUI.")
        .javafx(true)
        .launcher(WinTool::main)
        .help("""
        Given a script, this tool displays a JavaFX GUI.  The tool provides
        the Joe standard library along with the optional joe.console and
        joe.win packages.  See the Joe User's Guide for details.
        
        The tool searches for locally installed Joe packages on a user-provided
        library path, which must be a colon-delimited list of folder paths.
        By default, the library path is provided by the JOE_LIB_PATH environment
        variable (if defined); otherwise, the user can specify a library path
        via the --libpath option.
        
        The options are as follows:
        
        --libpath path, --l path
           Sets the library path to the given path.
        --clark, -c
            Use the "Clark" byte-engine (default)
        --walker, -w
            Use the "Walker" AST-walker engine.
        --debug, -d
            Enable debugging output.  This is mostly of use to
            the Joe maintainer.
        """)
        .build();

    //------------------------------------------------------------------------
    // Instance Variables

    // Keeps joe from being collected.
    private final Stage stage = new Stage();
    private final VBox root = new VBox();

    //------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the tool.
     */
    public WinTool() {
        // Nothing to do
    }

    //------------------------------------------------------------------------
    // Execution

    /**
     * Gets implementation info about the tool.
     * @return The info.
     */
    public ToolInfo toolInfo() {
        return INFO;
    }


    public void run(String[] args) {
        // FIRST, parse the command line arguments.
        var argq = new ArrayDeque<>(List.of(args));
        if (argq.isEmpty()) {
            printUsage(App.NAME);
            exit(64);
        }

        // NEXT, parse the options.
        var engineType = Joe.CLARK;
        String libPath = null;
        var debug = false;

        while (!argq.isEmpty() && argq.peek().startsWith("-")) {
            var opt = argq.poll();
            switch (opt) {
                case "--libpath", "-l" -> libPath = toOptArg(opt, argq);
                case "--clark", "-c" -> engineType = Joe.CLARK;
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
        stage.setTitle("joe win");
        Scene scene = new Scene(root, 400, 300);
//        scene.getStylesheets().add("file:foo.css");
        stage.setScene(scene);
        Platform.setImplicitExit(true);

        // NEXT, load the required packages
        var path = argq.poll();
        var consolePackage = new ConsolePackage();
        consolePackage.setScript(path);
        consolePackage.getArgs().addAll(argq);
        joe.installPackage(consolePackage);

        var guiPackage = new WinPackage(stage, root);
        joe.installPackage(guiPackage);
        var found = PackageFinder.find(libPath != null
            ? libPath
            : System.getenv(Joe.JOE_LIB_PATH));
        joe.registerPackages(found);

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
        var tool = new WinTool();
        tool.run(args);
    }
}
