package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.scene.Parent;

/**
 * Proxy type for the JavaFX Node widget.
 */
public class ParentType extends WidgetType<Parent> {
    /** The proxy type, for installation into an interpreter. */
    public static final ParentType TYPE = new ParentType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @widget Parent
    // %javaType javafx.scene.Parent
    // %proxyType com.wjduquette.joe.win.ParentType
    // %extends Widget
    // The `Parent` widget is the abstract base class for JavaFX widgets
    // that can own other widgets and have stylesheets.
    /** Constructor. */
    public ParentType() {
        super("Parent");
        extendsProxy(NodeType.TYPE);
        proxies(Parent.class);

        // No initializer

        // Methods
        method("getStylesheets", this::_getStylesheets);
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method getStylesheets
    // %result joe.List
    // Gets a list of the widget's CSS style sheets.  Sheets are specified
    // as paths to local files, or as `data:` URLs produced by
    // [[static:Win.css2sheet]].
    private Object _getStylesheets(Parent parent, Joe joe, Args args) {
        args.arity(0, "getStylesheets()");
        return joe.wrapList(parent.getStylesheets(), String.class);
    }
}
