package com.wjduquette.joe.win;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.TypeProxy;
import javafx.geometry.Insets;

class InsetsProxy extends TypeProxy<Insets> {
    public static final InsetsProxy TYPE = new InsetsProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type Insets
    // The `Insets` type used to set margins and padding around
    // [[Node]] widgets.
    public InsetsProxy() {
        super("Insets");
        proxies(Insets.class);

        // Initializer
        initializer(this::_initializer);

        // Methods
        method("getBottom", this::_getBottom);
        method("getLeft",   this::_getLeft);
        method("getRight",  this::_getRight);
        method("getTop",    this::_getTop);
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // @args pixels
    // @args top, right, bottom, left
    // Returns a `Insets`, which represents a margin on the four sides
    // of a [[Node]].  If a single value *pixels* is given, the margin will
    // be the same on all four sides; otherwise, the initializer expects
    // all four values in the given order.
    private Object _initializer(Joe joe, ArgQueue args) {
        return switch(args.size()) {
            case 1 -> new Insets(joe.toDouble(args.next()));
            case 4 -> new Insets(
                joe.toDouble(args.next()),
                joe.toDouble(args.next()),
                joe.toDouble(args.next()),
                joe.toDouble(args.next())
            );
            default -> throw Joe.arityFailure(
                "Insets(pixels) or Insets(top, right, bottom, left)");
        };
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method getBottom
    // @result Number
    // Gets the width of the margin on the bottom of the node, in pixels.
    private Object _getBottom(Insets insets, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "getBottom()");
        return insets.getBottom();
    }

    //**
    // @method getLeft
    // @result Number
    // Gets the width of the margin on the left of the node, in pixels.
    private Object _getLeft(Insets insets, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "getLeft()");
        return insets.getLeft();
    }

    //**
    // @method getRight
    // @result Number
    // Gets the width of the margin on the right of the node, in pixels.
    private Object _getRight(Insets insets, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "getRight()");
        return insets.getRight();
    }

    //**
    // @method getTop
    // @result Number
    // Gets the width of the margin on the top of the node, in pixels.
    private Object _getTop(Insets insets, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "getTop()");
        return insets.getTop();
    }
}
