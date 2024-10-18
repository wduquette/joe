package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import com.wjduquette.joe.types.EnumProxy;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * An experimental Joe package for building JavaFX GUIs
 */
public class WinPackage extends JoePackage {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Stage stage;
    private final VBox root;

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // This package contains an experimental API for building JavaFX GUIs.
    // It assumes a familiarity with JavaFX.

    public WinPackage(Stage stage, VBox root) {
        super("joe.win");
        this.stage = stage;
        this.root = root;

        // Main Singleton
        type(new WinProxy());

        // Base classes
        type(NodeProxy.TYPE);
        type(RegionProxy.TYPE);

        // Controls
        type(ControlProxy.TYPE);
        type(ButtonProxy.TYPE);
        type(LabelProxy.TYPE);

        // Panes
        type(PaneProxy.TYPE);
        type(VBoxProxy.TYPE);

        // Enums

        //**
        // @enum Pos
        // The `Pos` enum lists ways a widget can be aligned
        // relative to the boundaries of a rectangular space.
        //
        // @constant BASELINE_CENTER
        // @constant BASELINE_LEFT
        // @constant BASELINE_RIGHT
        // @constant BOTTOM_CENTER
        // @constant BOTTOM_LEFT
        // @constant BOTTOM_RIGHT
        // @constant CENTER
        // @constant CENTER_LEFT
        // @constant CENTER_RIGHT
        // @constant TOP_CENTER
        // @constant TOP_LEFT
        // @constant TOP_RIGHT
        type(new EnumProxy<>("Pos", Pos.class));


        //**
        // @enum Priority
        // The `Priority` enum's values indicate when a widget
        // should resize itself to fit its parent widget.  The
        // default is generally `NEVER`.
        //
        // @constant ALWAYS
        // @constant SOMETIMES
        // @constant NEVER
        type(new EnumProxy<>("Priority", Priority.class));

        // Utility Classes
        type(InsetsProxy.TYPE);
    }

    //**
    // @packageTopic hierarchy
    // @title Widget Hierarchy
    //
    // The `joe.win` widget type hierarchy is a subset of the JavaFX hierarchy.
    //
    // - [[Node]]: Base class
    //   - [[Region]]: Nodes with geometry
    //     - [[Control]]: Nodes to interact with
    //       - [[Button]]: A button
    //       - [[Label]]: A label
    //     - [[Pane]]: Nodes that manage children
    //       - [[VBox]]: A vertical stack of widgets

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
        // @result this
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
            return this;
        }

        //**
        // @static setTitle
        // @args title
        // @result this
        // Sets the title of the root window.
        private Object _setTitle(Joe joe, ArgQueue args) {
            Joe.exactArity(args, 1, "Win.setTitle(title)");
            var title = joe.toString(args.next());
            stage.setTitle(title);
            return this;
        }
    }

    //-------------------------------------------------------------------------
    // Static converters for use with properties

    static EventHandler<ActionEvent> toAction(Joe joe, Object arg) {
        return evt -> joe.call(arg, evt);
    }

    static Insets toInsets(Joe joe, Object arg) {
        return joe.toClass(arg, Insets.class);
    }

    static Pos toPos(Joe joe, Object arg) {
        return joe.toEnum(arg, Pos.class);
    }

    static Tooltip toTooltip(Joe joe, Object arg) {
        return joe.toClass(arg, Tooltip.class);
    }
}
