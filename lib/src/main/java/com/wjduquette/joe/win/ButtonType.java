package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.scene.control.Button;

/**
 * Proxy for a JavaFX Button.
 */
class ButtonType extends WidgetType<Button> {
    public static final ButtonType TYPE = new ButtonType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @widget Button
    // %extends Labeled
    // %javaType javafx.scene.control.Button
    // %proxyType com.wjduquette.joe.win.ButtonProxy
    // The `Button` type is the base class for JavaFX
    // labels like [[Button]] widgets.
    public ButtonType() {
        super("Button");
        extendsProxy(LabeledType.TYPE);
        proxies(Button.class);

        // Initializer
        initializer(this::_initializer);

        //**
        // @property onAction callable/1
        // See [[Button#method.onAction]].
        fxEvent("onAction", Button::onActionProperty);

        // Methods
        method("action", this::_action);
        method("onAction", this::_onAction);
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // %args [text]
    // Returns a `Button`. If the *text* is given, the button will display
    // the text.
    private Object _initializer(Joe joe, Args args) {
        args.arityRange(0, 1, "Button([text])");
        return args.isEmpty()
            ? new Button()
            : new Button(joe.stringify(args.next()));
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method action
    // %args callable
    // %result this
    // Adds a *callable/0* to the button as its `#onAction` handler; pressing
    // the button will invoke the callable.
    private Object _action(Button btn, Joe joe, Args args) {
        args.exactArity(1, "action(callable)");
        btn.setOnAction(Win.toActionNoArg(joe, args.next()));
        return btn;
    }

    //**
    // @method onAction
    // %args callable
    // %result this
    // Adds a *callable/1* to the button as its `#onAction` handler;
    // pressing the button will invoke the callable, passing it the JavaFX
    // `ActionEvent`.  Action event handlers rarely need the `ActionEvent`,
    // so it's often preferable to use [[Button#method.action]], which
    // expects a *callable/0*.
    private Object _onAction(Button btn, Joe joe, Args args) {
        args.exactArity(1, "onAction(callable)");
        btn.setOnAction(Win.toAction(joe, args.next()));
        return btn;
    }
}
