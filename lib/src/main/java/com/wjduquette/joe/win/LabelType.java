package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.scene.control.Label;

class LabelType extends WidgetType<Label> {
    public static final LabelType TYPE = new LabelType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type Label
    // @extends Control
    // The `Label` type is the base class for JavaFX
    // labels like [[Label]] widgets.
    public LabelType() {
        super("Label");
        extendsProxy(ControlType.TYPE);
        proxies(Label.class);

        // Initializer
        initializer(this::_initializer);

        //**
        // ## Properties
        //
        // `Label` widgets have the following properties, in addition to
        // those inherited from superclasses.
        //
        // | Property      | Type            | Description      |
        // | ------------- | --------------- | ---------------- |
        // | `#text`       | [[joe.String]]  | The label's text |

        // Properties
        fxProperty("text", Label::textProperty, Joe::toString);

        // Methods
        method("text", this::_text);
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // @args [text]
    // Returns a `Label`.
    private Object _initializer(Joe joe, Args args) {
        args.arityRange(0, 1, "Label([text])");
        if (args.isEmpty()) {
            return new Label();
        } else {
            return new Label(joe.stringify(args.next()));
        }
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method text
    // @args text
    // @result this
    // Sets the label's text.
    private Object _text(Label node, Joe joe, Args args) {
        args.exactArity(1, "text(text)");
        node.setText(joe.stringify(args.next()));
        return node;
    }
}
