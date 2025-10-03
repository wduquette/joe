package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.scene.control.MenuItem;

import java.util.stream.Collectors;

class MenuItemType extends WidgetType<MenuItem> {
    public static final MenuItemType TYPE = new MenuItemType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type MenuItem
    // @extends Widget
    // The `MenuItem` widget is an item in a [[Menu]].
    public MenuItemType() {
        super("MenuItem");
        proxies(MenuItem.class);

        initializer(this::_initializer);

        //**
        // ## Properties
        //
        // All `MenuItem` widgets have the following properties.
        //
        // | Property    | Type             | Description        |
        // | ----------- | ---------------- | ------------------ |
        // | `#id`       | [[joe.String]]   | JavaFX ID          |
        // | `#onAction` | *callable(1)*    | The `onAction` handler |
        // | `#style`    | [[joe.String]]   | FXCSS style string |
        // | `#text`     | [[joe.String]]   | MenuItem text      |
        //
        // See [[joe.win#topic.css]] for more on using CSS.
        fxProperty("id",       MenuItem::idProperty,      Joe::toString);
        fxProperty("onAction", MenuItem::onActionProperty, WinPackage::toAction);
        fxProperty("text",     MenuItem::textProperty,    Joe::toString);
        fxProperty("style",    MenuItem::styleProperty,   Joe::toString);

        // Methods
        method("action",        this::_action);
        method("disable",       this::_disable);
        method("id",            this::_id);
        method("isDisabled",    this::_isDisabled);
        method("styleClasses",  this::_styleClasses);
        method("styles",        this::_styles);
        method("text",          this::_text);
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Returns a `MenuItem`.
    private Object _initializer(Joe joe, Args args) {
        args.exactArity(0, "MenuItem()");
        return new MenuItem();
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method action
    // @args callable
    // @result this
    // Adds a no-arg *callable* to the menu item as its action; pressing the
    // button will invoke the callable.
    private Object _action(MenuItem item, Joe joe, Args args) {
        args.exactArity(1, "action(callable)");
        item.setOnAction(evt -> joe.call(args.next()));
        return item;
    }

    //**
    // @method disable
    // @args [flag]
    // @result this
    // Sets the item's `#disable` property to *flag*; if omitted,
    // *flag* defaults to `true`.  While `true`, this item and its
    // descendants in the scene graph will be disabled.
    private Object _disable(MenuItem item, Joe joe, Args args) {
        args.arityRange(0, 1, "disable([flag])");
        var flag = args.isEmpty() || Joe.isTruthy(args.next());
        item.setDisable(flag);
        return item;
    }

    //**
    // @method id
    // @args id
    // @result this
    // Sets the item's `#id` property to the given *id* string.
    private Object _id(MenuItem item, Joe joe, Args args) {
        args.exactArity(1, "id(id)");
        var id = joe.toString(args.next());
        item.setId(id);
        return item;
    }

    //**
    // @method isDisabled
    // @result joe.Boolean
    // Returns `true` if the item has been disabled, and `false` otherwise.
    private Object _isDisabled(MenuItem item, Joe joe, Args args) {
        args.exactArity(0, "isDisabled()");
        return item.isDisable();
    }

    //**
    // @method styleClasses
    // @result joe.List
    // Gets the list of the item's FXCSS style class names.  Values must
    // be valid CSS style class names.
    //
    // See [[joe.win#topic.css]] for more on using CSS.
    private Object _styleClasses(MenuItem item, Joe joe, Args args) {
        args.exactArity(0, "styleClasses()");
        return joe.wrapList(item.getStyleClass(), String.class);
    }

    //**
    // @method styles
    // @args style, ...
    // @result this
    // Sets the item's FXCSS `#style` property.  The caller can pass
    // multiple style strings, which will be joined with semicolons.
    //
    // See [[joe.win#topic.css]] for more on using CSS.
    private Object _styles(MenuItem item, Joe joe, Args args) {
        args.minArity(1, "styles(style, ...)");
        var styles = args.asList().stream()
            .map(joe::toString)
            .collect(Collectors.joining(";\n"));
        item.setStyle(styles);
        return item;
    }

    //**
    // @method text
    // @args text
    // @result this
    // Sets the item's text.
    private Object _text(MenuItem item, Joe joe, Args args) {
        args.exactArity(1, "text(text)");
        item.setText(joe.stringify(args.next()));
        return item;
    }
}
