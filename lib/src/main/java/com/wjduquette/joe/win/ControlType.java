package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;

class ControlType extends WidgetType<Control> {
    public static final ControlType TYPE = new ControlType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @widget Control
    // @extends Region
    // The `Control` type is the base class for JavaFX
    // controls like the [[Label]] and [[Button]] widgets.
    //
    // @typeTopic tooltips
    // @title Tooltip Text
    // The JavaFX `Tooltip` is a popup window with many properties.  For the
    // moment `Control` only supports `Tooltips` with a simple text string,
    // and so has a `#tooltipText` property but not a `#tooltip` property.
    public ControlType() {
        super("Control");
        extendsProxy(RegionType.TYPE);
        proxies(Control.class);

        //**
        // @property tooltipText joe.String
        // The control's tooltip string.
        fxProperty("tooltipText", Control::tooltipProperty,
            Win::toTooltip, Tooltip::getText);

        // Methods
        method("tooltipText", this::_tooltipText);
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method tooltipText
    // @args text
    // @result this
    // Gives the control a tooltip with the given *text*.
    private Object _tooltipText(Control node, Joe joe, Args args) {
        args.exactArity(1, "tooltipText(text)");
        node.setTooltip(new Tooltip(joe.stringify(args.next())));
        return node;
    }
}
