package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

class VBoxClass extends FXType<VBox> {
    public static final VBoxClass TYPE = new VBoxClass();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type VBox
    // @extends Pane
    // The `VBox` type is a [[Pane]] that manages a column of
    // widgets. Joe classes can extend the `VBox` type.
    public VBoxClass() {
        super("VBox");
        proxies(VBox.class);
        extendsProxy(PaneType.TYPE);

        staticMethod("getMargin", this::_getMargin);
        staticMethod("getVgrow",  this::_getVgrow);
        staticMethod("setMargin", this::_setMargin);
        staticMethod("setVgrow",  this::_setVgrow);

        // No initializer
        initializer(this::_initializer);

        //**
        // ## Properties
        //
        // `VBox` widgets have the following properties, in addition to
        // those inherited from superclasses.
        //
        // | Property     | Type           | Description                |
        // | ------------ | -------------- | -------------------------- |
        // | `#alignment` | [[Pos]]        | The default alignment for children  |
        // | `#spacing`   | [[joe.Number]] | The spacing between children in pixels |

        // Properties
        fxProperty("alignment", VBox::alignmentProperty, WinPackage::toPos);
        fxProperty("spacing",   VBox::spacingProperty,   Joe::toDouble);

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
        return new VBoxInstance(joeClass);
    }


    //-------------------------------------------------------------------------
    // Static Methods

    //**
    // @static getMargin
    // @args node
    // @result Insets
    // Gets the [[Node]]'s margin in its parent [[VBox]].
    private Object _getMargin(Joe joe, Args args) {
        args.exactArity(1, "VBox.getMargin(node)");
        return VBox.getMargin(joe.toClass(args.next(), Node.class));
    }

    //**
    // @static getVgrow
    // @args node
    // @result Priority
    // Gets how the [[Node]] will resize itself to the height of
    // its parent [[VBox]].
    private Object _getVgrow(Joe joe, Args args) {
        args.exactArity(1, "VBox.getVgrow(node)");
        return VBox.getVgrow(joe.toClass(args.next(), Node.class));
    }

    //**
    // @static setMargin
    // @args node, insets
    // Gets the [[Node]]'s margin in its parent [[VBox]] given an
    // [[Insets]] object.
    private Object _setMargin(Joe joe, Args args) {
        args.exactArity(2, "VBox.setMargin(node, insets)");
        VBox.setMargin(
            joe.toClass(args.next(), Node.class),
            joe.toClass(args.next(), Insets.class)
        );
        return null;
    }

    //**
    // @static setVgrow
    // @args node, priority
    // Sets how the [[Node]] will resize itself to the height of
    // its parent [[VBox]], given a [[Priority]] value.
    private Object _setVgrow(Joe joe, Args args) {
        args.exactArity(2, "VBox.setVgrow(node, priority)");
        VBox.setVgrow(
            joe.toClass(args.next(), Node.class),
            joe.toClass(args.next(), Priority.class)
        );
        return null;
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Returns a `VBox`.
    private Object _initializer(Joe joe, Args args) {
        args.exactArity(0, "VBox()");
        return make(joe, this);
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method spacing
    // @args pixels
    // @result this
    // Sets the vertical space in *pixels* between each child.
    private Object _spacing(VBox node, Joe joe, Args args) {
        args.exactArity(1, "spacing(pixels)");
        node.setSpacing(joe.toDouble(args.next()));
        return node;
    }
}
