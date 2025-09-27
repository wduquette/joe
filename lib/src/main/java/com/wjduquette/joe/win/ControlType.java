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
    // @type Control
    // @extends Region
    // The `Control` type is the base class for JavaFX
    // controls like the [[Label]] and [[Button]] widgets.
    public ControlType() {
        super("Control");
        extendsProxy(RegionType.TYPE);
        proxies(Control.class);

        //**
        // ## Properties
        //
        // `Control` widgets have the following properties, in addition to
        // those inherited from superclasses.
        //
        // | Property      | Type            | Description                |
        // | ------------- | --------------- | -------------------------- |
        // | `#tooltip`    | [[Tooltip]]     | The control's tooltip      |

        // Properties
        fxProperty("tooltip", Control::tooltipProperty, WinPackage::toTooltip);

        // Methods
        method("tooltip",     this::_tooltip);
        method("tooltipText", this::_tooltipText);
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method tooltip
    // @args tooltip
    // @result this
    // Sets the control's [[Tooltip]], or null for none.
    private Object _tooltip(Control node, Joe joe, Args args) {
        args.exactArity(0, "tooltip(tooltip)");
        node.setTooltip(joe.toClass(args.next(), Tooltip.class));
        return node;
    }

    //**
    // @method tooltipText
    // @args text
    // @result this
    // Gives the control a tooltip with the given *text*.
    private Object _tooltipText(Control node, Joe joe, Args args) {
        args.exactArity(0, "tooltipText(text)");
        node.setTooltip(new Tooltip(joe.stringify(args.next())));
        return node;
    }
}
