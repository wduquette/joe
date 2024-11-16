package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import com.wjduquette.joe.types.ListValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;

import java.util.List;

/**
 * Proxy for a JavaFX SplitPane.
 */
class SplitPaneProxy extends FXProxy<SplitPane> {
    public static final SplitPaneProxy TYPE = new SplitPaneProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type SplitPane
    // @extends Control
    // The `SplitPane` type manages one or more [[Node]] widgets with
    // movable dividers between them.
    //
    // If there are N children, then there are N-1 dividers between them,
    // indexed from 0 to N-2. The divider positions are fractions
    // between 0.0 and 1.0.
    public SplitPaneProxy() {
        super("SplitPane");
        extendsProxy(ControlProxy.TYPE);
        proxies(SplitPane.class);

        // Initializer
        initializer(this::_initializer);

        //**
        // ## Properties
        //
        // `SplitPane` widgets have the following properties, in addition to
        // those inherited from superclasses.
        //
        // | Property         | Type            | Description            |
        // | ---------------- | --------------- | ---------------------- |
        // | `#orientation`   | [[Orientation]] | Layout orientation     |

        fxProperty("orientation", SplitPane::orientationProperty, WinPackage::toOrientation);

        // Methods
        method("getDividers",      this::_getDividers);
        method("horizontal",       this::_horizontal);
        method("item",             this::_item);
        method("items",            this::_items);
        method("setDivider",       this::_setDivider);
        method("setDividers",      this::_setDividers);
        method("vertical",         this::_vertical);
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Returns a `SplitPane`.
    private Object _initializer(Joe joe, Args args) {
        args.exactArity(0, "SplitPane()");
        return new SplitPane();
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method getDividers
    // @result joe.List
    // Returns a list of the divider positions, which are fractions
    // between 0.0 and 1.0.
    private Object _getDividers(SplitPane node, Joe joe, Args args) {
        args.exactArity(0, "getDividers()");
        return new ListValue(List.of(node.getDividerPositions()));
    }

    //**
    // @method horizontal
    // @result this
    // Sets the orientation to horizontal
    private Object _horizontal(SplitPane node, Joe joe, Args args) {
        args.exactArity(0, "horizontal()");
        node.setOrientation(Orientation.HORIZONTAL);
        return node;
    }

    //**
    // @method item
    // @args item
    // @result this
    // Adds a value to the widget's list of [[Node]] widgets.
    private Object _item(SplitPane node, Joe joe, Args args) {
        args.exactArity(1, "item(item)");
        node.getItems().add(WinPackage.toNode(joe, args.next()));
        return node;
    }

    //**
    // @method items
    // @result joe.List
    // Gets the list of the widget's items, which can be updated freely.
    // All items must be [[Node]] widgets.
    private Object _items(SplitPane node, Joe joe, Args args) {
        args.exactArity(0, "items()");
        return joe.wrapList(node.getItems(), Node.class);
    }

    //**
    // @method setDivider
    // @args index position
    // @result this
    // Sets the value of divider with the given *index* to the given
    // position.
    private Object _setDivider(SplitPane node, Joe joe, Args args) {
        args.exactArity(1, "setDivider(index)");
        var index = joe.toIndex(args.next(), node.getItems().size() - 2);
        var position = toFraction(joe, args.next());
        node.setDividerPosition(index, position);
        return node;
    }

    //**
    // @method setDividers
    // @args position,...
    // @result this
    // Sets the value of all dividers to the given positions.
    private Object _setDividers(SplitPane node, Joe joe, Args args) {
        args.minArity(1, "setDividers(positions)");
        var numbers = new double[args.size()];
        for (int i = 0; i < args.size(); i++) {
            numbers[i] = toFraction(joe, args.get(i));
        }

        if (numbers.length > node.getDividers().size()) {
            throw new JoeError("Expected no more than " +
                node.getDividers().size() +
                " positions, got: " + numbers.length + ".");
        }

        node.setDividerPositions(numbers);

        return node;
    }

    //**
    // @method vertical
    // @result this
    // Sets the orientation to vertical
    private Object _vertical(SplitPane node, Joe joe, Args args) {
        args.exactArity(0, "vertical()");
        node.setOrientation(Orientation.VERTICAL);
        return node;
    }

    private double toFraction(Joe joe, Object value) {
        var position = joe.toDouble(value);
        if (position < 0.0 || position > 1.0) {
            throw joe.expected("fraction between 0.0 and 1.0", value);
        }
        return position;
    }
}
