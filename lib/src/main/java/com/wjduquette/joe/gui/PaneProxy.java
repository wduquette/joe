package com.wjduquette.joe.gui;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.TypeProxy;
import javafx.scene.Node;
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
        extendsProxy(RegionProxy.TYPE);
        proxies(Pane.class);

        // No initializer
        initializer(this::_initializer);

        // Methods
        method("child", this::_child);
        method("getChildren", this::_getChildren);
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
    // @method child
    // @args node
    // @result this
    // Adds a [[Node]] to the end of the pane's *children* list.
    private Object _child(Pane pane, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "child(node)");
        pane.getChildren().add(joe.toClass(args.next(), Node.class));
        return pane;
    }

    //**
    // @method getChildren
    // @result joe.List
    // Gets the list of the node's children, which can be updated freely.
    // All items must belong some [[Node]] subclass.
    private Object _getChildren(Pane pane, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "getChildren()");
        return joe.wrapList(pane.getChildren(), Node.class);
    }
}
