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
    // @extends Labeled
    // The `Label` widget.
    public LabelType() {
        super("Label");
        extendsProxy(LabeledType.TYPE);
        proxies(Label.class);

        // Initializer
        initializer(this::_initializer);
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
}
