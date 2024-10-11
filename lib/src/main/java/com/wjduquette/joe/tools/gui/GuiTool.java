package com.wjduquette.joe.tools.gui;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.app.App;
import com.wjduquette.joe.console.ConsolePackage;
import com.wjduquette.joe.tools.FXTool;
import com.wjduquette.joe.tools.ToolInfo;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Deque;

public class GuiTool extends FXTool {
    public static final ToolInfo INFO = new ToolInfo(
        "gui",
        "script.joe args...",
        "Displays a scripted GUI.",
        """
        Given a script, this tool displays a JavaFX GUI.  The tool provides
        the Joe standard library along with the optional joe.console and
        joe.gui packages.  See the Joe User's Guide for details.
        """,
        GuiTool::main
    );

    //------------------------------------------------------------------------
    // Instance Variables

    private final VBox root = new VBox();

    //------------------------------------------------------------------------
    // Constructor

    public GuiTool() {
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

        // NEXT, load the script.
        var joe = new Joe();
        var path = args.poll();

        var consolePackage = new ConsolePackage();
        consolePackage.getArgs().addAll(args);
        joe.installPackage(consolePackage);

        try {
            joe.runFile(path);
        } catch (IOException ex) {
            System.err.println("Could not read script: " + path +
                "\n*** " + ex.getMessage());
            System.exit(1);
        } catch (SyntaxError ex) {
            ex.printErrorsByLine();
            System.err.println(ex.getMessage());
            System.exit(65);
        } catch (JoeError ex) {
            if (ex.line() >= 0) {
                System.err.print("[line " + ex.line() + "] ");
            }
            System.err.println(ex.getJoeStackTrace());
            System.exit(70);
        }

        // NEXT, pop up the window
        Scene scene = new Scene(root, 400, 300);

        stage.setTitle("joe gui");
        stage.setScene(scene);
        stage.show();
    }

    //------------------------------------------------------------------------
    // Main

    public static void main(String[] args) {
        launch(args);
    }
}
