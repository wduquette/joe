package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.scene.control.Button;

/**
 * Proxy for a JavaFX Button.
 */
class ButtonType extends FXType<Button> {
    public static final ButtonType TYPE = new ButtonType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type Button
    // @extends Control
    // The `Button` type is the base class for JavaFX
    // labels like [[Button]] widgets.
    public ButtonType() {
        super("Button");
        extendsProxy(ControlType.TYPE);
        proxies(Button.class);

        // Initializer
        initializer(this::_initializer);

        //**
        // ## Properties
        //
        // `Button` widgets have the following properties, in addition to
        // those inherited from superclasses.
        //
        // | Property      | Type            | Description            |
        // | ------------- | --------------- | ---------------------- |
        // | `#onAction`   | *callable(1)*   | The `onAction` handler |
        // | `#text`       | [[joe.String]]  | The button's text      |
        //
        // - *callable(1)*: A callable taking one argument

        fxProperty("onAction", Button::onActionProperty, WinPackage::toAction);
        fxProperty("text",     Button::textProperty,     Joe::toString);

        // Methods
        method("action", this::_action);
        method("text",   this::_text);
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // @args [text], [action]
    // Returns a `Button`. If the *text* is given, the button will display
    // the text.  If the *action* is also given, it must be a no-arg
    // callable; it will be invoked when the button is pressed.
    private Object _initializer(Joe joe, Args args) {
        args.arityRange(0, 2, "Button([text],[action])");
        return switch(args.size()) {
            case 0 -> new Button();
            case 1 -> new Button(joe.stringify(args.next()));
            case 2 -> {
                var btn = new Button(joe.stringify(args.next()));
                btn.setOnAction(evt -> joe.call(args.next()));
                yield btn;
            }
            default -> throw new IllegalStateException("Bad range.");
        };
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method action
    // @args callable
    // @result this
    // Adds a no-arg *callable* to the button as its action; pressing the
    // button will invoke the callable.
    private Object _action(Button node, Joe joe, Args args) {
        args.exactArity(1, "action(callable)");
        node.setOnAction(evt -> joe.call(args.next()));
        return node;
    }

    //**
    // @method text
    // @args text
    // @result this
    // Sets the button's *text*.
    private Object _text(Button node, Joe joe, Args args) {
        args.exactArity(1, "text(text)");
        node.setText(joe.stringify(args.next()));
        return node;
    }
}
