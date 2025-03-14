package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeClass;
import com.wjduquette.joe.JoeValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

class StackPaneClass extends FXType<StackPane> {
    public static final StackPaneClass TYPE = new StackPaneClass();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type StackPane
    // @extends Pane
    // The `StackPane` type is a [[Pane]] that manages one or children
    // stacked one on top of each other like cards in a deck.
    // Joe classes can extend the `StackPane` type.
    public StackPaneClass() {
        super("StackPane");
        proxies(StackPane.class);
        extendsProxy(PaneType.TYPE);

        staticMethod("getAlignment", this::_getAlignment);
        staticMethod("getMargin",    this::_getMargin);
        staticMethod("setAlignment", this::_setAlignment);
        staticMethod("setMargin",    this::_setMargin);

        // No initializer
        initializer(this::_initializer);

        //**
        // ## Properties
        //
        // `StackPane` widgets have the following properties, in addition to
        // those inherited from superclasses.
        //
        // | Property     | Type           | Description                |
        // | ------------ | -------------- | -------------------------- |
        // | `#alignment` | [[Pos]]        | The default alignment for children  |

        // Properties
        fxProperty("alignment", StackPane::alignmentProperty, WinPackage::toPos);

        // Methods
        // None
    }

    //-------------------------------------------------------------------------
    // JoeClass API

    @Override
    public boolean canBeExtended() {
        return true;
    }

    @Override
    public JoeValue make(Joe joe, JoeClass joeClass) {
        return new StackPaneInstance(joeClass);
    }


    //-------------------------------------------------------------------------
    // Static Methods

    //**
    // @static getAlignment
    // @args node
    // @result Pos
    // Gets the [[Node]]'s alignment in its parent [[StackPane]].
    private Object _getAlignment(Joe joe, Args args) {
        args.exactArity(1, "StackPane.getAlignment(node)");
        return StackPane.getAlignment(joe.toClass(args.next(), Node.class));
    }

    //**
    // @static getMargin
    // @args node
    // @result Insets
    // Gets the [[Node]]'s margin in its parent [[StackPane]].
    private Object _getMargin(Joe joe, Args args) {
        args.exactArity(1, "StackPane.getMargin(node)");
        return StackPane.getMargin(joe.toClass(args.next(), Node.class));
    }

    //**
    // @static setAlignment
    // @args node, pos
    // Sets how [[Node]] *node* will position itself within its
    // parent [[StackPane]], given a [[Pos]] value.
    private Object _setAlignment(Joe joe, Args args) {
        args.exactArity(2, "StackPane.setAlignment(node, pos)");
        StackPane.setAlignment(
            joe.toClass(args.next(), Node.class),
            joe.toClass(args.next(), Pos.class)
        );
        return null;
    }

    //**
    // @static setMargin
    // @args node, insets
    // Gets the [[Node]]'s margin in its parent [[StackPane]] given an
    // [[Insets]] object.
    private Object _setMargin(Joe joe, Args args) {
        args.exactArity(2, "StackPane.setMargin(node, insets)");
        StackPane.setMargin(
            joe.toClass(args.next(), Node.class),
            joe.toClass(args.next(), Insets.class)
        );
        return null;
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Returns a `StackPane`.
    private Object _initializer(Joe joe, Args args) {
        args.exactArity(0, "StackPane()");
        return make(joe, this);
    }

    //-------------------------------------------------------------------------
    // Methods

    // None

}
