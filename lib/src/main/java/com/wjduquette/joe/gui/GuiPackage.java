package com.wjduquette.joe.gui;

import com.wjduquette.joe.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * An experimental Joe package for building JavaFX GUIs
 */
public class GuiPackage extends JoePackage {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Stage stage;
    private final VBox root;

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.gui
    // This package contains an experimental API for building JavaFX GUIs.

    public GuiPackage(Stage stage, VBox root) {
        super("joe.gui");
        this.stage = stage;
        this.root = root;

        type(new WinProxy());
    }

    //-------------------------------------------------------------------------
    // Configuration

    // None Yet


    //-------------------------------------------------------------------------
    // The Gui Type

    private class WinProxy extends TypeProxy<Void> {
        //---------------------------------------------------------------------
        // Constructor

        //**
        // @type Win
        // This static type provides access to the application window.
        WinProxy() {
            super("Win");
            staticType();
            staticMethod("root",  this::_root);
            staticMethod("setSize",  this::_setSize);
            staticMethod("setTitle", this::_setTitle);
        }

        //---------------------------------------------------------------------
        // Static Methods

        //**
        // @static root
        // @result VBox
        // Returns the root window, a [[VBox]].
        private Object _root(Joe joe, ArgQueue args) {
            Joe.exactArity(args, 0, "Win.root()");
            return root;
        }

        //**
        // @static setSize
        // @args width, height
        // Sets the preferred size of the root window.  The width and height
        // must be positive.
        private Object _setSize(Joe joe, ArgQueue args) {
            Joe.exactArity(args, 2, "Win.setSize(width, title)");
            var width = joe.toDouble(args.next());
            var height = joe.toDouble(args.next());
            if (width <= 0 || height <= 0) {
                throw new JoeError("Expected positive width and height.");
            }
            stage.setWidth(width);
            stage.setHeight(height);
            return null;
        }

        //**
        // @static setTitle
        // @args title
        // Sets the title of the root window.
        private Object _setTitle(Joe joe, ArgQueue args) {
            Joe.exactArity(args, 1, "Win.setTitle(title)");
            var title = joe.toString(args.next());
            stage.setTitle(title);
            return null;
        }
    }
}
