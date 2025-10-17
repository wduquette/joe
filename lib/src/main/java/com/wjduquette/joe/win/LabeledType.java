package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.scene.control.Labeled;

class LabeledType extends WidgetType<Labeled> {
    public static final LabeledType TYPE = new LabeledType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @widget Labeled
    // @extends Control
    // The `Labeled` type is the base class for JavaFX widgets that display
    // text and an optional graphic, e.g., [[Label]] and [[Button]].
    public LabeledType() {
        super("Labeled");
        extendsProxy(ControlType.TYPE);
        proxies(Labeled.class);

        //**
        // @property text Joe.string
        // The text to display
        fxProperty("text", Labeled::textProperty, Joe::toString);

        // Methods
        method("text", this::_text);
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method text
    // @args text
    // @result this
    // Sets the labeled's text.
    private Object _text(Labeled node, Joe joe, Args args) {
        args.exactArity(1, "text(text)");
        node.setText(joe.stringify(args.next()));
        return node;
    }
}
