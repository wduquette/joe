package com.wjduquette.joe.gui;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.TypeProxy;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

class LabelProxy extends TypeProxy<Label> {
    public static final LabelProxy TYPE = new LabelProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.gui
    // @type Label
    // The `Label` type is the base class for JavaFX
    // labels like [[Label]] widgets.
    public LabelProxy() {
        super("Label");
        extendsProxy(ControlProxy.TYPE);
        proxies(Label.class);

        // Initializer
        initializer(this::_initializer);

        // Methods
        method("getText", this::_getText);
        method("setText", this::_setText);
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // @args [text]
    // Returns a `Label`.
    private Object _initializer(Joe joe, ArgQueue args) {
        Joe.arityRange(args, 0, 1, "Label([text])");
        if (args.isEmpty()) {
            return new Label();
        } else {
            return new Label(joe.stringify(args.next()));
        }
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method getText
    // @result joe.String
    // Gets the label's text, or null for none.
    private Object _getText(Label node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "getText()");
        return node.getText();
    }

    //**
    // @method setText
    // @args text
    // @result this
    // Gets the label's text or null for none.
    private Object _setText(Label node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "setText(text)");
        node.setText(joe.stringify(args.next()));
        return node;
    }
}
