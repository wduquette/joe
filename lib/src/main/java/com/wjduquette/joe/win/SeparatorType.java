package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;

class SeparatorType extends FXType<Separator> {
    public static final SeparatorType TYPE = new SeparatorType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type Separator
    // @extends Control
    // The [[Separator]] widget is a horizontal or vertical separator.
    public SeparatorType() {
        super("Separator");
        extendsProxy(ControlType.TYPE);
        proxies(Separator.class);

        // Initializer
        initializer(this::_initializer);

        //**
        // ## Properties
        //
        // `Separator` widgets have the following properties, in addition to
        // those inherited from superclasses.
        //
        // | Property       | Type            | Description            |
        // | -------------- | --------------- | ---------------------- |
        // | `#orientation` | [[Orientation]] | Horizontal or vertical |

        // Properties
        fxProperty("orientation", Separator::orientationProperty, WinPackage::toOrientation);

        // Methods
        method("horizontal", this::_horizontal);
        method("vertical",   this::_vertical);
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Returns a `Separator`, which is horizontal by default.
    private Object _initializer(Joe joe, Args args) {
        args.exactArity(0, "Separator()");
        return new Separator();
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method horizontal
    // @result this
    // Sets the orientation to horizontal
    private Object _horizontal(Separator node, Joe joe, Args args) {
        args.exactArity(0, "horizontal()");
        node.setOrientation(Orientation.HORIZONTAL);
        return node;
    }

    //**
    // @method vertical
    // @result this
    // Sets the orientation to vertical
    private Object _vertical(Separator node, Joe joe, Args args) {
        args.exactArity(0, "vertical()");
        node.setOrientation(Orientation.VERTICAL);
        return node;
    }
}
