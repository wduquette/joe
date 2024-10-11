package com.wjduquette.joe.gui;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.TypeProxy;
import javafx.scene.layout.Pane;

class PaneProxy extends TypeProxy<Pane> {
    public static final PaneProxy TYPE = new PaneProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.gui
    // @type Pane
    // The `Pane` type is the base class for JavaFX
    // [[Node]] widgets that can have child nodes.
    public PaneProxy() {
        super("Pane");
        proxies(Pane.class);

        // No initializer
        initializer(this::_initializer);

        // Methods
        method("getGetChildren", this::_getChildren);
    }


    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Returns a `Pane`.
    private Object _initializer(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "Pane()");
        return new Pane();
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method getChildren
    // @result joe.List
    // Gets the list of the node's children, which can be updated freely.
    // All items must belong some [[Node]] subclass.
    private Object _getChildren(Pane node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "getChildren()");
        return node.getChildren();
    }
}
