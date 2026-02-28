package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.scene.Scene;

/**
 * Proxy type for the JavaFX Scene widget.
 */
public class SceneType extends WidgetType<Scene> {
    /** Proxy type for installation into an interpreter. */
    public static final SceneType TYPE = new SceneType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @widget Scene
    // %javaType javafx.scene.Scene
    // %proxyType com.wjduquette.joe.win.SceneType
    // The `Scene` type is the top-level widget of any JavaFX GUI, and contains
    // a tree of [[Node|Nodes]] called the *scene graph*.  A `Scene` is
    // displayed in a [[Window]], usually a [[Stage]].
    /** Constructor. */
    public SceneType() {
        super("Scene");
        proxies(Scene.class);

        // Properties

        // TODO: Lots of event handlers

        initializer(this::_init);

        // Methods
        method("getRoot",        this::_getRoot);
        method("getStylesheets", this::_getStylesheets);

        // TODO userData, setRoot, lookup, event handler methods
    }

    //-------------------------------------------------------------------------
    // Initializers


    //**
    // @init
    // %args root, [width, height]
    // Creates a new `Scene`, specifying the *root* widget (a subclass of
    // [[Parent]]) and optionally the *width* and *height*.
    private Object _init(Joe joe, Args args) {
        if (args.size() == 1) {
            return new Scene(Win.toParent(joe, args.next()));
        } else if (args.size() == 3) {
            return new Scene(
                Win.toParent(joe, args.next()),
                joe.toDouble(args.next()),
                joe.toDouble(args.next())
            );
        } else {
            throw Args.arityFailure("Scene(root) or Scene(root, width, height");
        }
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method getRoot
    // %result Parent
    // Gets the scene's root widget.
    private Object _getRoot(Scene scene, Joe joe, Args args) {
        args.arity(0, "getRoot()");
        return scene.getRoot();
    }

    //**
    // @method getStylesheets
    // %result joe.List
    // Gets a list of the scene's CSS style sheets.  Sheets are specified
    // as paths to local files, or as `data:` URLs produced by
    // [[static:Win.css2sheet]].
    private Object _getStylesheets(Scene scene, Joe joe, Args args) {
        args.arity(0, "getStylesheets()");
        return joe.wrapList(scene.getStylesheets(), String.class);
    }
}
