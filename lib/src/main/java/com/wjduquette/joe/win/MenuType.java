package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

/**
 * Proxy type for the JavaFX Menu widget.
 */
public class MenuType extends WidgetType<Menu> {
    /** The proxy type, for installation into an interpreter. */
    public static final MenuType TYPE = new MenuType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @widget Menu
    // %javaType javafx.scene.control.Menu
    // %proxyType com.wjduquette.joe.win.MenuType
    // %extends MenuItem
    // The `Menu` widget is a menu in a [[MenuBar]] or a submenu
    // in a parent [[Menu]].  It contains [[MenuItem]] widgets.
    /** Constructor. */
    public MenuType() {
        super("Menu");
        extendsProxy(MenuItemType.TYPE);
        proxies(Menu.class);

        initializer(this::_initializer);

        // Methods
        method("item",          this::_item);
        method("items",         this::_items);
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
    // @method item
    // %args item
    // %result this
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
    // %result joe.List
    // Gets the list of the menu's items, which can be updated freely.
    // All items must be instances of [[MenuItem]] or [[Menu]].
    private Object _items(Menu bar, Joe joe, Args args) {
        args.exactArity(0, "items()");
        // Note: All Menus are also MenuItems.
        return joe.wrapList(bar.getItems(), MenuItem.class);
    }
}
