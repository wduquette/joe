package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

class GridPaneClass extends WidgetType<GridPane> {
    public static final GridPaneClass TYPE = new GridPaneClass();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @widget GridPane
    // @extends Pane
    // The `GridPane` type is a [[Pane]] that positions its children within
    // a grid of rows and columns.  Children can occupy a single cell or
    // span multiple rows and/or columns. Joe classes can extend the
    // `GridPane` type.
    public GridPaneClass() {
        super("GridPane");
        proxies(GridPane.class);
        extendsProxy(PaneType.TYPE);

        // Static Methods (more to come)
        staticMethod("getHgrow",  this::_getHgrow);
        staticMethod("getMargin", this::_getMargin);
        staticMethod("getVgrow",  this::_getVgrow);
        staticMethod("setHgrow",  this::_setHgrow);
        staticMethod("setMargin", this::_setMargin);
        staticMethod("setVgrow",  this::_setVgrow);

        // No initializer
        initializer(this::_initializer);

        //**
        // @property alignment Pos
        // Overall alignment of content
        fxProperty("alignment", GridPane::alignmentProperty, Win::toPos);

        //**
        // @property gridLinesVisible joe.Boolean
        // Draw grid lines for debugging
        fxProperty("gridLinesVisible", GridPane::gridLinesVisibleProperty, Joe::toBoolean);

        //**
        // @property hgap joe.Number
        // Gap between columns in pixels
        fxProperty("hgap", GridPane::hgapProperty, Joe::toDouble);

        //**
        // @property vgap joe.Number
        // Gap between rows in pixels
        fxProperty("vgap", GridPane::vgapProperty, Joe::toDouble);

        // Methods
        method("at",   this::_at);
        method("hgap", this::_hgap);
        method("vgap", this::_vgap);
    }

    //-------------------------------------------------------------------------
    // JoeClass API

    @Override
    public boolean canBeExtended() {
        return true;
    }

    @Override
    public Object make(Joe joe, JoeClass joeClass) {
        return new GridPaneInstance(joeClass);
    }

    //-------------------------------------------------------------------------
    // Static Methods

    //**
    // @static getHgrow
    // @args node
    // @result Priority
    // Gets how the [[Node]] will resize itself to the height of
    // its parent `GridPane`.
    private Object _getHgrow(Joe joe, Args args) {
        args.exactArity(1, "GridPane.getHgrow(node)");
        return GridPane.getHgrow(joe.toClass(args.next(), Node.class));
    }


    //**
    // @static getMargin
    // @args node
    // @result Insets
    // Gets the [[Node]]'s margin in its parent `GridPane`.
    private Object _getMargin(Joe joe, Args args) {
        args.exactArity(1, "GridPane.getMargin(node)");
        return GridPane.getMargin(joe.toClass(args.next(), Node.class));
    }

    //**
    // @static getVgrow
    // @args node
    // @result Priority
    // Gets how the [[Node]] will resize itself to the height of
    // its parent `GridPane`.
    private Object _getVgrow(Joe joe, Args args) {
        args.exactArity(1, "GridPane.getVgrow(node)");
        return GridPane.getVgrow(joe.toClass(args.next(), Node.class));
    }

    //**
    // @static setHgrow
    // @args node, priority
    // Sets how the [[Node]] will resize itself to the height of
    // its parent `GridPane, given a [[Priority]] value.
    private Object _setHgrow(Joe joe, Args args) {
        args.exactArity(2, "GridPane.setHgrow(node, priority)");
        GridPane.setHgrow(
            joe.toClass(args.next(), Node.class),
            joe.toClass(args.next(), Priority.class)
        );
        return null;
    }

    //**
    // @static setMargin
    // @args node, insets
    // Gets the [[Node]]'s margin in its parent `GridPane` given an
    // [[Insets]] object.
    private Object _setMargin(Joe joe, Args args) {
        args.exactArity(2, "GridPane.setMargin(node, insets)");
        GridPane.setMargin(
            joe.toClass(args.next(), Node.class),
            joe.toClass(args.next(), Insets.class)
        );
        return null;
    }

    //**
    // @static setVgrow
    // @args node, priority
    // Sets how the [[Node]] will resize itself to the height of
    // its parent `GridPane`, given a [[Priority]] value.
    private Object _setVgrow(Joe joe, Args args) {
        args.exactArity(2, "GridPane.setVgrow(node, priority)");
        GridPane.setVgrow(
            joe.toClass(args.next(), Node.class),
            joe.toClass(args.next(), Priority.class)
        );
        return null;
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Returns a `GridPane`.
    private Object _initializer(Joe joe, Args args) {
        args.exactArity(0, "GridPane()");
        return make(joe, this);
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method at
    // @args column, row, [columnSpan, rowSpan], node
    // @result this
    // Adds the *node* to the grid pane at the given *column* and *row*
    // with the given spans.  If omitted, the spans default to 1.
    private Object _at(GridPane pane, Joe joe, Args args) {
        if (args.size() == 3) {
            var column = joe.toInteger(args.next());
            var row = joe.toInteger(args.next());
            var node = Win.toNode(joe, args.next());
            GridPane.setConstraints(node, column, row);
            pane.getChildren().add(node);
        } else if (args.size() == 5) {
            var column = joe.toInteger(args.next());
            var row = joe.toInteger(args.next());
            var columnSpan = Win.toSpan(joe, args.next());
            var rowSpan = Win.toSpan(joe, args.next());
            var node = Win.toNode(joe, args.next());
            GridPane.setConstraints(node, column, row, columnSpan, rowSpan);
            pane.getChildren().add(node);
        } else {
            throw Args.arityFailure("at(column, row, [columnSpan, rowSpan], node)");
        }

        return pane;
    }

    //**
    // @method hgap
    // @args pixels
    // @result this
    // Sets the gap between columns to the given number of *pixels*.
    private Object _hgap(GridPane pane, Joe joe, Args args) {
        args.exactArity(1, "hgap(pixels)");
        pane.setHgap(joe.toDouble(args.next()));
        return pane;
    }

    //**
    // @method vgap
    // @args pixels
    // @result this
    // Sets the gap between rows to the given number of *pixels*.
    private Object _vgap(GridPane pane, Joe joe, Args args) {
        args.exactArity(1, "vgap(pixels)");
        pane.setVgap(joe.toDouble(args.next()));
        return pane;
    }
}
