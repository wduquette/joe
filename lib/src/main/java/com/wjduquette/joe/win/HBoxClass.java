package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.HBox;

class HBoxClass extends WidgetType<HBox> {
    public static final HBoxClass TYPE = new HBoxClass();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @widget HBox
    // @extends Pane
    // The `HBox` type is a [[Pane]] that manages a row of
    // widgets. Joe classes can extend the `HBox` type.
    public HBoxClass() {
        super("HBox");
        proxies(HBox.class);
        extendsProxy(PaneType.TYPE);

        staticMethod("getHgrow",  this::_getHgrow);
        staticMethod("getMargin", this::_getMargin);
        staticMethod("setHgrow",  this::_setHgrow);
        staticMethod("setMargin", this::_setMargin);

        // No initializer
        initializer(this::_initializer);

        //**
        // @property alignment Pos
        // Alignment for children.
        fxProperty("alignment", HBox::alignmentProperty, Win::toPos);

        //**
        // @property spacing joe.Number
        // Spacing between children in pixels
        fxProperty("spacing",   HBox::spacingProperty,   Joe::toDouble);

        // Methods
        method("spacing", this::_spacing);
    }

    //-------------------------------------------------------------------------
    // JoeClass API

    @Override
    public boolean canBeExtended() {
        return true;
    }

    @Override
    public Object make(Joe joe, JoeClass joeClass) {
        return new HBoxInstance(joeClass);
    }

    //-------------------------------------------------------------------------
    // Static Methods

    //**
    // @static getHgrow
    // @args node
    // @result Priority
    // Gets how the [[Node]] will resize itself to the height of
    // its parent `HBox`.
    private Object _getHgrow(Joe joe, Args args) {
        args.exactArity(1, "HBox.getHgrow(node)");
        return HBox.getHgrow(joe.toClass(args.next(), Node.class));
    }

    //**
    // @static getMargin
    // @args node
    // @result Insets
    // Gets the [[Node]]'s margin in its parent `HBox`.
    private Object _getMargin(Joe joe, Args args) {
        args.exactArity(1, "HBox.getMargin(node)");
        return HBox.getMargin(joe.toClass(args.next(), Node.class));
    }

    //**
    // @static setHgrow
    // @args node, priority
    // Sets how the [[Node]] will resize itself to the height of
    // its parent `HBox`, given a [[Priority]] value.
    private Object _setHgrow(Joe joe, Args args) {
        args.exactArity(2, "HBox.setHgrow(node, priority)");
        HBox.setHgrow(
            joe.toClass(args.next(), Node.class),
            joe.toClass(args.next(), Priority.class)
        );
        return null;
    }

    //**
    // @static setMargin
    // @args node, insets
    // Gets the [[Node]]'s margin in its parent `HBox` given an
    // [[Insets]] object.
    private Object _setMargin(Joe joe, Args args) {
        args.exactArity(2, "HBox.setMargin(node, insets)");
        HBox.setMargin(
            joe.toClass(args.next(), Node.class),
            joe.toClass(args.next(), Insets.class)
        );
        return null;
    }


    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Returns an `HBox`.
    private Object _initializer(Joe joe, Args args) {
        args.exactArity(0, "HBox()");
        return make(joe, this);
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method spacing
    // @args pixels
    // @result this
    // Sets the vertical space in *pixels* between each child.
    private Object _spacing(HBox node, Joe joe, Args args) {
        args.exactArity(1, "spacing(pixels)");
        node.setSpacing(joe.toDouble(args.next()));
        return node;
    }
}
