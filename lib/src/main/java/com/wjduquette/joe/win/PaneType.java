package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

class PaneType extends WidgetType<Pane> {
    public static final PaneType TYPE = new PaneType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @widget Pane
    // %extends Region
    // The `Pane` type is the base class for JavaFX
    // widgets that manage child [[Node|Nodes]].
    public PaneType() {
        super("Pane");
        extendsProxy(RegionType.TYPE);
        proxies(Pane.class);

        // No initializer
        initializer(this::_initializer);

        // Methods
        method("child",       this::_child);
        method("children",    this::_children);
        method("setChildren", this::_setChildren);
    }


    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Returns a `Pane`.
    private Object _initializer(Joe joe, Args args) {
        args.exactArity(0, "Pane()");
        return new Pane();
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method child
    // %args node
    // %result this
    // Adds a [[Node]] to the end of the pane's *children* list.
    private Object _child(Pane pane, Joe joe, Args args) {
        args.exactArity(1, "child(node)");
        pane.getChildren().add(joe.toClass(args.next(), Node.class));
        return pane;
    }

    //**
    // @method children
    // %result joe.List
    // Gets the list of the node's children, which can be updated freely.
    // All items must belong some [[Node]] subclass.
    private Object _children(Pane pane, Joe joe, Args args) {
        args.exactArity(0, "children()");
        return joe.wrapList(pane.getChildren(), Node.class);
    }

    //**
    // @method setChildren
    // %args list
    // %result this
    // Replaces the widget's [[Pane#method.children]] with those from
    // the given [[joe.List]].  All children must be [[Node|Nodes]].
    private Object _setChildren(Pane pane, Joe joe, Args args) {
        args.exactArity(1, "setChildren(list)");
        pane.getChildren().clear();
        for (var child : joe.toList(args.next())) {
            pane.getChildren().add(joe.toClass(child, Node.class));
        }
        return pane;
    }
}
