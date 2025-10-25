package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

class TabPaneType extends WidgetType<TabPane> {
    public static final TabPaneType TYPE = new TabPaneType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type TabPane
    // %extends Control
    // The `TabPane` type is the base class for JavaFX tab panes, which can
    // contain [[Tab]] objects.
    public TabPaneType() {
        super("TabPane");
        extendsProxy(ControlType.TYPE);
        proxies(TabPane.class);

        // Initializer
        initializer(this::_initializer);

        //**
        // @property rotateGraphic joe.Boolean
        // Rotate graphic to match `#side`
        fxProperty("rotateGraphic", TabPane::rotateGraphicProperty, Joe::toBoolean);

        //**
        // @property side Side
        // Position for the [[Tab|Tabs]]
        fxProperty("side", TabPane::sideProperty,
            (joe, value) -> joe.toEnum(value, Side.class));

        // Methods
        method("tab",  this::_tab);
        method("tabs", this::_tabs);
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // %args [text]
    // Returns a `TabPane`.
    private Object _initializer(Joe joe, Args args) {
        args.exactArity(0, "TabPane()");
        return new TabPane();
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method tab
    // %args tab
    // %result this
    // Adds a [[Tab]] to the pane.
    private Object _tab(TabPane pane, Joe joe, Args args) {
        args.exactArity(1, "tab(tab)");
        var tab = joe.toClass(args.next(), Tab.class);
        pane.getTabs().add(tab);
        return pane;
    }

    //**
    // @method tabs
    // %result joe.List
    // Gets the list of the pane's tabs, which can be updated freely.
    // All items must be instances of [[Tab]] or a subclass.
    private Object _tabs(TabPane pane, Joe joe, Args args) {
        args.exactArity(0, "tabs()");
        return joe.wrapList(pane.getTabs(), Tab.class);
    }
}
