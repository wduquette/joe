package com.wjduquette.joe.win;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.TypeProxy;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;

class ControlProxy extends TypeProxy<Control> {
    public static final ControlProxy TYPE = new ControlProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type Control
    // @extends Region
    // The `Control` type is the base class for JavaFX
    // controls like [[Label]] widgets.
    public ControlProxy() {
        super("Control");
        extendsProxy(RegionProxy.TYPE);
        proxies(Control.class);

        // Methods
        method("getTooltip",     this::_getTooltip);
        method("setTooltip",     this::_setTooltip);
        method("setTooltipText", this::_setTooltipText);
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method getTooltip
    // @result Tooltip
    // Gets the control's tooltip, or null for none.
    private Object _getTooltip(Control node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "getTooltip()");
        return node.getTooltip();
    }

    //**
    // @method setTooltip
    // @args tooltip
    // @result this
    // Gets the control's [[Tooltip]], or null for none.
    private Object _setTooltip(Control node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "setTooltip(tooltip)");
        node.setTooltip(joe.toClass(args.next(), Tooltip.class));
        return node;
    }

    //**
    // @method setTooltipText
    // @args text
    // @result this
    // Gives the control a tooltip with the given *text*.
    private Object _setTooltipText(Control node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "setTooltipText(text)");
        node.setTooltip(new Tooltip(joe.stringify(args.next())));
        return node;
    }
}
