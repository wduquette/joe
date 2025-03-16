package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeClass;
import com.wjduquette.joe.JoeValue;
import javafx.scene.layout.GridPane;

class GridPaneClass extends FXType<GridPane> {
    public static final GridPaneClass TYPE = new GridPaneClass();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type GridPane
    // @extends Pane
    // The `GridPane` type is a [[Pane]] that manages one or children
    // stacked one on top of each other like cards in a deck.
    // Joe classes can extend the `GridPane` type.
    public GridPaneClass() {
        super("GridPane");
        proxies(GridPane.class);
        extendsProxy(PaneType.TYPE);

        // No initializer
        initializer(this::_initializer);

        //**
        // ## Properties
        //
        // `GridPane` widgets have the following properties, in addition to
        // those inherited from superclasses.
        //
        // | Property            | Type            | Description                |
        // | ------------------- | --------------- | -------------------------- |
        // | `#alignment`        | [[Pos]]         | Alignment of the grid within the widget. |
        // | `#gridLinesVisible` | [[joe.Boolean]] | Whether to draw grid lines for debugging. |
        // | `#hgap`             | [[joe.Number]]  | Gap between columns in pixels. |
        // | `#vgap`             | [[joe.Number]]  | Gap between rows in pixels. |

        // Properties
        fxProperty("alignment", GridPane::alignmentProperty, WinPackage::toPos);
        fxProperty("gridLinesVisible", GridPane::gridLinesVisibleProperty, Joe::toBoolean);
        fxProperty("hgap", GridPane::hgapProperty, Joe::toDouble);
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
    public JoeValue make(Joe joe, JoeClass joeClass) {
        return new GridPaneInstance(joeClass);
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
            var node = WinPackage.toNode(joe, args.next());
            GridPane.setConstraints(node, column, row);
            pane.getChildren().add(node);
        } else if (args.size() == 5) {
            var column = joe.toInteger(args.next());
            var row = joe.toInteger(args.next());
            var columnSpan = WinPackage.toSpan(joe, args.next());
            var rowSpan = WinPackage.toSpan(joe, args.next());
            var node = WinPackage.toNode(joe, args.next());
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
