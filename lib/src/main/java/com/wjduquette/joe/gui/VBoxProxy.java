package com.wjduquette.joe.gui;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.TypeProxy;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

class VBoxProxy extends TypeProxy<VBox> {
    public static final VBoxProxy TYPE = new VBoxProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.gui
    // @type VBox
    // The `VBox` type is a [[Pane]] that manages a vertical stack of
    // widgets.
    public VBoxProxy() {
        super("VBox");
        proxies(VBox.class);
        extendsProxy(PaneProxy.TYPE);

        // No initializer
        initializer(this::_initializer);

        // Methods
        method("getAlignment", this::_getAlignment);
        method("isFillWidth",  this::_isFillWidth);
        method("getSpacing",   this::_getSpacing);
        method("setAlignment", this::_setAlignment);
        method("setFillWidth",  this::_setFillWidth);
        method("setSpacing",   this::_setSpacing);
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
    // @method getAlignment
    // @result Pos
    // Gets the default alignment of children within the box.
    private Object _getAlignment(VBox node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "getAlignment()");
        return node.getAlignment();
    }

    //**
    // @method getSpacing
    // @result joe.Number
    // Gets the vertical space in pixels between each child.
    private Object _getSpacing(VBox node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "getSpacing()");
        return node.getSpacing();
    }

    //**
    // @method isFillWidth
    // @result joe.Boolean
    // Gets whether children will fill the width of the vbox or not.
    private Object _isFillWidth(VBox node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "isFillWidth()");
        return node.isFillWidth();
    }

    //**
    // @method setAlignment
    // @args pos
    // @result this
    // Sets the default alignment of children within the box to the
    // given [[Pos]] value.
    private Object _setAlignment(VBox node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "setAlignment(pos)");
        node.setAlignment(joe.toEnum(args.next(), Pos.class));
        return node;
    }

    //**
    // @method setFillWith
    // @args flag
    // @result this
    // Sets whether children will fill the width of the vbox or not.
    private Object _setFillWidth(VBox node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "setFillWidth(flag)");
        node.setFillWidth(Joe.isTruthy(args.next()));
        return node;
    }

    //**
    // @method setSpacing
    // @args pixels
    // @result this
    // Sets the vertical space in *pixels* between each child.
    private Object _setSpacing(VBox node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "setSpacing(pixels)");
        node.setSpacing(joe.toDouble(args.next()));
        return node;
    }
}
