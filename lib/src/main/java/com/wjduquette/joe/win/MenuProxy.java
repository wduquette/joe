package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.util.stream.Collectors;

class MenuProxy extends FXProxy<Menu> {
    public static final MenuProxy TYPE = new MenuProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type Menu
    // The `Menu` widget is a menu in a [[MenuBar]] or a submenu
    // in a parent [[Menu]].  It contains [[MenuItem]] widgets.
    //
    // @includeMixin FXProxy
    public MenuProxy() {
        super("Menu");
        proxies(Menu.class);

        initializer(this::_initializer);

        //**
        // ## Properties
        //
        // All `Node` widgets have the following properties.
        //
        // | Property    | Type             | Description        |
        // | ----------- | ---------------- | ------------------ |
        // | `#id`       | [[joe.String]]   | JavaFX ID          |
        // | `#style`    | [[joe.String]]   | FXCSS style string |
        // | `#text`     | [[joe.String]]   | Menu text      |
        //
        // See [[joe.win#topic.css]] for more on using CSS.
        fxProperty("id",       Menu::idProperty,      Joe::toString);
        fxProperty("text",     Menu::textProperty,    Joe::toString);
        fxProperty("style",    Menu::styleProperty,   Joe::toString);

        // Methods
        method("disable",       this::_disable);
        method("id",            this::_id);
        method("isDisabled",    this::_isDisabled);
        method("item",          this::_item);
        method("items",         this::_items);
        method("styleClasses",  this::_styleClasses);
        method("styles",        this::_styles);
        method("text",          this::_text);
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Returns a `Menu`.
    private Object _initializer(Joe joe, Args args) {
        args.exactArity(0, "Menu()");
        return new Menu();
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method disable
    // @args [flag]
    // @result this
    // Sets the menu's `#disable` property to *flag*; if omitted,
    // *flag* defaults to `true`.  While `true`, this menu and its
    // descendants in the scene graph will be disabled.
    private Object _disable(Menu menu, Joe joe, Args args) {
        args.arityRange(0, 1, "disable([flag])");
        var flag = args.isEmpty() || Joe.isTruthy(args.next());
        menu.setDisable(flag);
        return menu;
    }

    //**
    // @method id
    // @args id
    // @result this
    // Sets the menu's `#id` property to the given *id* string.
    private Object _id(Menu menu, Joe joe, Args args) {
        args.exactArity(1, "id(id)");
        var id = joe.toString(args.next());
        menu.setId(id);
        return menu;
    }

    //**
    // @method isDisabled
    // @result joe.Boolean
    // Returns `true` if the menu has been disabled, and `false` otherwise.
    private Object _isDisabled(Menu menu, Joe joe, Args args) {
        args.exactArity(0, "isDisabled()");
        return menu.isDisable();
    }

    //**
    // @method item
    // @args item
    // @result this
    // Adds a [[MenuItem]] or [[Menu]] to the menu.
    private Object _item(Menu bar, Joe joe, Args args) {
        args.exactArity(1, "item(item)");
        // Note: All Menus are also MenuItems.
        var item = joe.toClass(args.next(), MenuItem.class);
        bar.getItems().add(item);
        return bar;
    }

    //**
    // @method items
    // @result joe.List
    // Gets the list of the menu's items, which can be updated freely.
    // All items must be instances of [[MenuItem]] or [[Menu]].
    private Object _items(Menu bar, Joe joe, Args args) {
        args.exactArity(0, "items()");
        // Note: All Menus are also MenuItems.
        return joe.wrapList(bar.getItems(), MenuItem.class);
    }

    //**
    // @method styleClasses
    // @result joe.List
    // Gets the list of the menu's FXCSS style class names.  Values must
    // be valid CSS style class names.
    //
    // See [[joe.win#topic.css]] for more on using CSS.
    private Object _styleClasses(Menu menu, Joe joe, Args args) {
        args.exactArity(0, "styleClasses()");
        return joe.wrapList(menu.getStyleClass(), String.class);
    }

    //**
    // @method styles
    // @args style, ...
    // @result this
    // Sets the menu's FXCSS `#style` property.  The caller can pass
    // multiple style strings, which will be joined with semicolons.
    //
    // See [[joe.win#topic.css]] for more on using CSS.
    private Object _styles(Menu menu, Joe joe, Args args) {
        args.minArity(1, "styles(style, ...)");
        var styles = args.asList().stream()
            .map(joe::toString)
            .collect(Collectors.joining(";\n"));
        menu.setStyle(styles);
        return menu;
    }

    //**
    // @method text
    // @args text
    // @result this
    // Sets the menu's text.
    private Object _text(Menu menu, Joe joe, Args args) {
        args.exactArity(1, "text(text)");
        menu.setText(joe.stringify(args.next()));
        return menu;
    }
}
