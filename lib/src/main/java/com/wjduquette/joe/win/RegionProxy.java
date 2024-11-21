package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;

class RegionProxy extends FXProxy<Region> {
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
        // Use as the `#minWidth`, `#maxWidth`, `#minHeight`, or `#maxHeight`
        // value to indicate that the preferred width or height should be used
        // for that property.
        constant("USE_PREF_SIZE",     Region.USE_PREF_SIZE);

        //**
        // ## Properties
        //
        // `Region` widgets have the following properties, in addition to
        // those inherited from superclasses.
        //
        // | Property      | Type            | Description                |
        // | ------------- | --------------- | -------------------------- |
        // | `#maxHeight`  | [[joe.Number]]  | Maximum height in pixels   |
        // | `#maxWidth`   | [[joe.Number]]  | Maximum width in pixels    |
        // | `#minHeight`  | [[joe.Number]]  | Minimum height in pixels   |
        // | `#minWidth`   | [[joe.Number]]  | Minimum width in pixels    |
        // | `#padding`    | [[Insets]]      | Preferred height in pixels |
        // | `#prefHeight` | [[joe.Number]]  | Preferred height in pixels |
        // | `#prefWidth`  | [[joe.Number]]  | Preferred width in pixels  |

        // Properties
        fxProperty("maxHeight",  Region::maxHeightProperty,  Joe::toDouble);
        fxProperty("maxHeight",  Region::maxHeightProperty,  Joe::toDouble);
        fxProperty("minWidth",   Region::minWidthProperty,   Joe::toDouble);
        fxProperty("minWidth",   Region::minWidthProperty,   Joe::toDouble);
        fxProperty("padding",    Region::paddingProperty,    WinPackage::toInsets);
        fxProperty("prefHeight", Region::prefHeightProperty, Joe::toDouble);
        fxProperty("prefWidth",  Region::prefWidthProperty,  Joe::toDouble);

        // No initializer

        // Methods
        method("height",         this::_height);
        method("padding",        this::_padding);
        method("prefWidth",      this::_prefWidth);
        method("prefHeight",     this::_prefHeight);
        method("prefWidth",      this::_prefWidth);
        method("width",          this::_width);
    }


    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method height
    // @args height
    // @result this
    // Sets the node's preferred, minimum, and maximum height in pixels.
    private Object _height(Region node, Joe joe, Args args) {
        args.exactArity(1, "height(height)");
        var pixels = joe.toDouble(args.next());
        node.setMinHeight(pixels);
        node.setMaxHeight(pixels);
        node.setPrefHeight(pixels);
        return node;
    }

    //**
    // @method padding
    // @args pixels
    // @args top, right, bottom, left
    // @result this
    // Sets the padding in pixels on all sides of the region.
    // If a single value is given, it is used for all four sides.
    private Object _padding(Region node, Joe joe, Args args) {
        var insets = switch(args.size()) {
            case 1 -> new Insets(joe.toDouble(args.next()));
            case 4 -> new Insets(
                joe.toDouble(args.next()),
                joe.toDouble(args.next()),
                joe.toDouble(args.next()),
                joe.toDouble(args.next())
            );
            default -> throw Args.arityFailure(
                "padding(pixels) or padding(top, right, bottom, left)");
        };

        node.setPadding(insets);
        return node;
    }

    //**
    // @method prefHeight
    // @args height
    // @result this
    // Sets the node's preferred height in pixels.
    private Object _prefHeight(Region node, Joe joe, Args args) {
        args.exactArity(1, "prefHeight(height)");
        node.setPrefHeight(joe.toDouble(args.next()));
        return node;
    }

    //**
    // @method prefWidth
    // @args width
    // @result this
    // Sets the node's preferred width in pixels.
    private Object _prefWidth(Region node, Joe joe, Args args) {
        args.exactArity(1, "prefWidth(width)");
        node.setPrefWidth(joe.toDouble(args.next()));
        return node;
    }

    //**
    // @method width
    // @args width
    // @result this
    // Sets the node's preferred, minimum, and maximum width in pixels.
    private Object _width(Region node, Joe joe, Args args) {
        args.exactArity(1, "width(width)");
        var pixels = joe.toDouble(args.next());
        node.setMinWidth(pixels);
        node.setMaxWidth(pixels);
        node.setPrefWidth(pixels);
        return node;
    }
}
