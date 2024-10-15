package com.wjduquette.joe.win;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

class VBoxProxy extends FXProxy<VBox> {
    public static final VBoxProxy TYPE = new VBoxProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type VBox
    // @extends Pane
    // The `VBox` type is a [[Pane]] that manages a vertical stack of
    // widgets.
    public VBoxProxy() {
        super("VBox");
        proxies(VBox.class);
        extendsProxy(PaneProxy.TYPE);

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
        // | Property     | Type       | Description                |
        // | ------------ | ---------- | -------------------------- |
        // | `#alignment` | [[Pos]]    | The default alignment for children  |
        // | `#spacing`   | [[Number]] | The spacing between children in pixels |

        // Properties
        fxProperty("alignment", Pos.class, VBox::alignmentProperty, WinPackage::toPos);
        fxProperty("spacing", Number.class, VBox::spacingProperty, Joe::toDouble);

        // Methods
        method("spacing", this::_spacing);
    }

    //-------------------------------------------------------------------------
    // Static Methods

    //**
    // @static getMargin
    // @args node
    // @result Insets
    // Gets the [[Node]]'s margin in its parent [[VBox]].
    private Object _getMargin(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "VBox.getMargin(node)");
        return VBox.getMargin(joe.toClass(args.next(), Node.class));
    }

    //**
    // @static getVgrow
    // @args node
    // @result Priority
    // Gets how the [[Node]] will resize itself to the height of
    // its parent [[VBox]].
    private Object _getVgrow(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "VBox.getVgrow(node)");
        return VBox.getVgrow(joe.toClass(args.next(), Node.class));
    }

    //**
    // @static setMargin
    // @args node, insets
    // Gets the [[Node]]'s margin in its parent [[VBox]] given an
    // [[Insets]] object.
    private Object _setMargin(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 2, "VBox.setMargin(node, insets)");
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
    private Object _setVgrow(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 2, "VBox.setVgrow(node, priority)");
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
    private Object _initializer(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "VBox()");
        return new VBox();
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method spacing
    // @args pixels
    // @result this
    // Sets the vertical space in *pixels* between each child.
    private Object _spacing(VBox node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "spacing(pixels)");
        node.setSpacing(joe.toDouble(args.next()));
        return node;
    }
}
