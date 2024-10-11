package com.wjduquette.joe.gui;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.TypeProxy;
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

        // No initializer
        initializer(this::_initializer);

        // Methods
//        method("getAlignment", this::_getAlignment);
//        method("isFillWidth",  this::_isFillWidth);
        method("getSpacing",   this::_getSpacing);
//        method("setAlignment", this::_getAlignment);
//        method("setFillWidth",  this::_isFillWidth);
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
    // @method getSpacing
    // @result joe.Number
    // Gets the vertical space in pixels between each child.
    private Object _getSpacing(VBox node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "getSpacing()");
        return node.getSpacing();
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
