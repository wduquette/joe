package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;

class RegionType extends WidgetType<Region> {
    public static final RegionType TYPE = new RegionType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @widget Region
    // @extends Node
    // The `Region` type is the abstract base class for JavaFX
    // [[Node]] widgets that occupy space on the screen.
    public RegionType() {
        super("Region");
        extendsProxy(NodeType.TYPE);
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


        // Properties

        //**
        // @property height joe.Number
        // Current height in pixels (read-only).
        fxReadOnly("height", Region::heightProperty);

        //**
        // @property maxHeight joe.Number
        // Maximum height in pixels.
        fxProperty("maxHeight",  Region::maxHeightProperty,  Joe::toDouble);

        //**
        // @property maxWidth joe.Number
        // Maximum width in pixels.
        fxProperty("maxWidth",   Region::maxWidthProperty,   Joe::toDouble);

        //**
        // @property minHeight joe.Number
        // Minimum height in pixels.
        fxProperty("minHeight",  Region::minHeightProperty,  Joe::toDouble);

        //**
        // @property minWidth joe.Number
        // Minimum width in pixels.
        fxProperty("minWidth",   Region::minWidthProperty,   Joe::toDouble);

        //**
        // @property padding Insets
        // Padding around the widget.
        fxProperty("padding",    Region::paddingProperty,    WinPackage::toInsets);

        //**
        // @property prefHeight joe.Number
        // Preferred height in pixels.
        fxProperty("prefHeight", Region::prefHeightProperty, Joe::toDouble);

        //**
        // @property prefWidth joe.Number
        // Preferred width in pixels.
        fxProperty("prefWidth",  Region::prefWidthProperty,  Joe::toDouble);

        //**
        // @property width joe.Number
        // Current width in pixels (read-only).
        fxReadOnly("width", Region::widthProperty);

        // No initializer

        // Methods
        method("getHeight",      this::_getHeight);
        method("getWidth",       this::_getWidth);
        method("height",         this::_height);
        method("padding",        this::_padding);
        method("maxHeight",      this::_maxHeight);
        method("maxWidth",       this::_maxWidth);
        method("minHeight",      this::_minHeight);
        method("minWidth",       this::_minWidth);
        method("prefHeight",     this::_prefHeight);
        method("prefWidth",      this::_prefWidth);
        method("width",          this::_width);
    }


    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method getHeight
    // @result joe.Number
    // Gets the node's `#height` in pixels.
    private Object _getHeight(Region node, Joe joe, Args args) {
        args.exactArity(0, "getHeight()");
        return node.getHeight();
    }

    //**
    // @method getWidth
    // @result joe.Number
    // Gets the node's `#width` in pixels.
    private Object _getWidth(Region node, Joe joe, Args args) {
        args.exactArity(0, "getWidth()");
        return node.getWidth();
    }

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
    // @method maxHeight
    // @args height
    // @result this
    // Sets the node's maximum height in pixels.
    private Object _maxHeight(Region node, Joe joe, Args args) {
        args.exactArity(1, "maxHeight(height)");
        node.setMaxHeight(joe.toDouble(args.next()));
        return node;
    }

    //**
    // @method maxWidth
    // @args width
    // @result this
    // Sets the node's maximum width in pixels.
    private Object _maxWidth(Region node, Joe joe, Args args) {
        args.exactArity(1, "maxWidth(width)");
        node.setMaxWidth(joe.toDouble(args.next()));
        return node;
    }

    //**
    // @method minHeight
    // @args height
    // @result this
    // Sets the node's minimum height in pixels.
    private Object _minHeight(Region node, Joe joe, Args args) {
        args.exactArity(1, "minHeight(height)");
        node.setMinHeight(joe.toDouble(args.next()));
        return node;
    }

    //**
    // @method minWidth
    // @args width
    // @result this
    // Sets the node's minimum width in pixels.
    private Object _minWidth(Region node, Joe joe, Args args) {
        args.exactArity(1, "minWidth(width)");
        node.setMinWidth(joe.toDouble(args.next()));
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
