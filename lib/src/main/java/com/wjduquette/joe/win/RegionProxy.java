package com.wjduquette.joe.win;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.TypeProxy;
import javafx.scene.layout.Region;

class RegionProxy extends TypeProxy<Region> {
    public static final RegionProxy TYPE = new RegionProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type Region
    // @extends Node
    // The `Region` type is the abstract base class for JavaFX
    // [[Node]] widgets that occupy space on the screen.
    public RegionProxy() {
        super("Region");
        extendsProxy(NodeProxy.TYPE);
        proxies(Region.class);

        // Constants

        //**
        // @constant USE_COMPUTED_SIZE
        //
        // Default setting for the region's various width and height
        // properties.
        constant("USE_COMPUTED_SIZE", Region.USE_COMPUTED_SIZE);

        //**
        // @constant USE_PREF_SIZE
        //
        // Use as the `minWidth`, `maxWidth`, `minHeight`, or `maxHeight`
        // value to indicate that the preferred width or height should be used
        // for that property.
        constant("USE_PREF_SIZE",     Region.USE_PREF_SIZE);

        // No initializer

        // Methods
        method("getPrefHeight",     this::_getPrefHeight);
        method("setPrefHeight",     this::_setPrefHeight);
        method("getPrefWidth",      this::_getPrefWidth);
        method("setPrefWidth",      this::_setPrefWidth);

        // PROPERTIES TO ADD
        // maxWidth, minWidth, maxHeight, minHeight,
        // width, height (setting these sets max, min, preferred)
        // padding (requires Insets type)
    }


    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method getPrefHeight
    // @result joe.Number
    // Gets the node's preferred height in pixels.
    private Object _getPrefHeight(Region node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "getPrefHeight()");
        return node.getPrefHeight();
    }

    //**
    // @method getPrefWidth
    // @result joe.Number
    // Gets the node's preferred width in pixels.
    private Object _getPrefWidth(Region node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "getPrefWidth()");
        return node.getPrefWidth();
    }

    //**
    // @method setPrefHeight
    // @args height
    // @result this
    // Sets the node's preferred height in pixels.
    private Object _setPrefHeight(Region node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "setPrefHeight(height)");
        node.setPrefHeight(joe.toDouble(args.next()));
        return node;
    }

    //**
    // @method setPrefWidth
    // @args width
    // @result this
    // Sets the node's preferred width in pixels.
    private Object _setPrefWidth(Region node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "setPrefWidth(width)");
        node.setPrefWidth(joe.toDouble(args.next()));
        return node;
    }
}
