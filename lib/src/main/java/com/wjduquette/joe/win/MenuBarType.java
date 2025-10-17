package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;

class MenuBarType extends WidgetType<MenuBar> {
    public static final MenuBarType TYPE = new MenuBarType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @widget MenuBar
    // @extends Control
    // The `MenuBar` type represents a horizontal menu bar.
    public MenuBarType() {
        super("MenuBar");
        extendsProxy(ControlType.TYPE);
        proxies(MenuBar.class);

        // Initializer
        initializer(this::_initializer);

        //**
        // @property useSystemMenuBar joe.Boolean
        // Display as system menu bar on macOS.
        fxProperty("useSystemMenuBar", MenuBar::useSystemMenuBarProperty, Joe::toBoolean);

        // Methods
        method("menu",  this::_menu);
        method("menus", this::_menus);
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // @args [text]
    // Returns a `MenuBar`.
    private Object _initializer(Joe joe, Args args) {
        args.exactArity(0, "MenuBar()");
        return new MenuBar();
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method menu
    // @args menu
    // @result this
    // Adds a [[Menu]] to the menu bar.
    private Object _menu(MenuBar bar, Joe joe, Args args) {
        args.exactArity(1, "menu(menu)");
        var menu = joe.toClass(args.next(), Menu.class);
        bar.getMenus().add(menu);
        return bar;
    }

    //**
    // @method menus
    // @result joe.List
    // Gets the list of the menu bar's menus, which can be updated freely.
    // All items must be instances of [[Menu]].
    private Object _menus(MenuBar bar, Joe joe, Args args) {
        args.exactArity(0, "menus()");
        return joe.wrapList(bar.getMenus(), Menu.class);
    }
}
