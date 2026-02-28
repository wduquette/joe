package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Proxy type for the JavaFX Stage widget.
 */
public class StageType extends WidgetType<Stage> {
    /** Proxy type for installation into an interpreter. */
    public static final StageType TYPE = new StageType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @widget Stage
    // %javaType javafx.stage.Stage
    // %proxyType com.wjduquette.joe.win.StageType
    // The `Stage` type is JavaFX application window type.  A `Stage`
    // contains a [[Scene]], which defines the actual GUI.
    /** Constructor. */
    public StageType() {
        super("Stage");
        extendsProxy(WindowType.TYPE);
        proxies(Stage.class);

        // Properties
        //**
        // @property maxHeight joe.Number
        // Maximum height in pixels.
        fxProperty("maxHeight", Stage::maxHeightProperty, Joe::toDouble);

        //**
        // @property maxWidth joe.Number
        // Maximum width in pixels.
        fxProperty("maxWidth", Stage::maxWidthProperty, Joe::toDouble);

        //**
        // @property minHeight joe.Number
        // Minimum height in pixels.
        fxProperty("minHeight", Stage::minHeightProperty, Joe::toDouble);

        //**
        // @property minWidth joe.Number
        // Minimum width in pixels.
        fxProperty("minWidth", Stage::minWidthProperty, Joe::toDouble);

        //**
        // @property resizable joe.Boolean
        // Whether the stage is resizable by the user.
        fxProperty("resizable", Stage::resizableProperty, Joe::toBoolean);

        //**
        // @property title joe.String
        // The stage's window title.
        fxProperty("title", Stage::titleProperty, Joe::toString);

        initializer(this::_init);

        // Methods
        method("getModality",  this::_getModality);
        method("getOwner",     this::_getOwner);
        method("getStyle",     this::_getStyle);
        method("getTitle",     this::_getTitle);
        method("initModality", this::_initModality);
        method("initOwner",    this::_initOwner);
        method("initStyle",    this::_initStyle);
        method("isResizable",  this::_isResizable);
        method("maxHeight",    this::_maxHeight);
        method("maxWidth",     this::_maxWidth);
        method("minHeight",    this::_minHeight);
        method("minWidth",     this::_minWidth);
        method("show",         this::_show);
        method("showAndWait",  this::_showAndWait);
        method("resizable",    this::_resizable);
        method("scene",        this::_scene);
        method("title",        this::_title);
        method("toBack",       this::_toBack);
        method("toFront",      this::_toFront);
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Creates a new [[Stage]].  The application will now terminate when
    // the final window is closed.
    private Object _init(Joe joe, Args args) {
        args.arity(0, "Stage()");

        Platform.setImplicitExit(true);
        return new Stage();
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method getModality
    // %result Modality
    // Gets the stage's [[Modality]].
    private Object _getModality(Stage stage, Joe joe, Args args) {
        args.arity(0, "getModality()");
        return stage.getModality();
    }

    //**
    // @method getOwner
    // %result Window
    // Gets the stage's owning window, or null for top-level windows.
    private Object _getOwner(Stage stage, Joe joe, Args args) {
        args.arity(0, "getOwner()");
        return stage.getOwner();
    }

    //**
    // @method getStyle
    // %result StageStyle
    // Gets the stage's decoration style.
    private Object _getStyle(Stage stage, Joe joe, Args args) {
        args.arity(0, "getStyle()");
        return stage.getStyle();
    }

    //**
    // @method getTitle
    // %result joe.String
    // Gets the stage's window title.
    private Object _getTitle(Stage stage, Joe joe, Args args) {
        args.arity(0, "getTitle()");
        return stage.getTitle();
    }

    //**
    // @method initModality
    // %args value
    // %result this
    // Sets the stage's [[Modality]].  This must be set before the
    // stage is first shown.
    private Object _initModality(Stage stage, Joe joe, Args args) {
        args.arity(1, "initModality(value)");
        stage.initModality(joe.toEnum(Modality.class, args.next()));
        return stage;
    }

    //**
    // @method initOwner
    // %args window
    // %result this
    // Sets the stage's owning [[Window]] for subordinate windows.
    private Object _initOwner(Stage stage, Joe joe, Args args) {
        args.arity(1, "initOwner(value)");
        stage.initOwner(joe.toType(Window.class, args.next()));
        return stage;
    }

    //**
    // @method initStyle
    // %args style
    // %result this
    // Sets the stage's [[StageStyle]].
    private Object _initStyle(Stage stage, Joe joe, Args args) {
        args.arity(1, "initStyle(value)");
        stage.initStyle(joe.toEnum(StageStyle.class, args.next()));
        return stage;
    }

    //**
    // @method isResizable
    // %result joe.Boolean
    // Gets whether the stage is resizable by the user or not.
    private Object _isResizable(Stage stage, Joe joe, Args args) {
        args.arity(0, "isResizable()");
        return stage.isResizable();
    }

    //**
    // @method maxHeight
    // %args height
    // %result this
    // Sets the stage's maximum height in pixels.
    private Object _maxHeight(Stage stage, Joe joe, Args args) {
        args.arity(1, "maxHeight(height)");
        stage.setMaxHeight(joe.toDouble(args.next()));
        return stage;
    }

    //**
    // @method maxWidth
    // %args width
    // %result this
    // Sets the stage's maximum width in pixels.
    private Object _maxWidth(Stage stage, Joe joe, Args args) {
        args.arity(1, "maxWidth(width)");
        stage.setMaxWidth(joe.toDouble(args.next()));
        return stage;
    }

    //**
    // @method minHeight
    // %args height
    // %result this
    // Sets the stage's minimum height in pixels.
    private Object _minHeight(Stage stage, Joe joe, Args args) {
        args.arity(1, "minHeight(height)");
        stage.setMinHeight(joe.toDouble(args.next()));
        return stage;
    }

    //**
    // @method minWidth
    // %args width
    // %result this
    // Sets the stage's minimum width in pixels.
    private Object _minWidth(Stage stage, Joe joe, Args args) {
        args.arity(1, "minWidth(width)");
        stage.setMinWidth(joe.toDouble(args.next()));
        return stage;
    }

    //**
    // @method show
    // %result this
    // Pops up the stage.
    private Object _show(Stage stage, Joe joe, Args args) {
        args.arity(0, "show()");
        stage.show();
        return stage;
    }

    //**
    // @method showAndWait
    // %result this
    // Pops up the stage and waits until the user closes it.
    private Object _showAndWait(Stage stage, Joe joe, Args args) {
        args.arity(0, "showAndWait()");
        stage.showAndWait();
        return stage;
    }

    //**
    // @method resizable
    // %args flag
    // %result this
    // Sets whether the stage is resizable by the user or not.  It is
    // resizable by default.
    private Object _resizable(Stage stage, Joe joe, Args args) {
        args.arity(1, "resizable(flag)");
        stage.setResizable(joe.toBoolean(args.next()));
        return stage;
    }

    //**
    // @method scene
    // %args scene
    // %result this
    // Sets the stage's [[Scene]], which defines the displays GUI.
    private Object _scene(Stage stage, Joe joe, Args args) {
        args.arity(1, "scene(scene)");
        stage.setScene(joe.toType(Scene.class, args.next()));
        return stage;
    }

    //**
    // @method title
    // %args title
    // %result this
    // Sets the stage's window title.
    private Object _title(Stage stage, Joe joe, Args args) {
        args.arity(1, "title(title)");
        stage.setTitle(joe.toString(args.next()));
        return stage;
    }

    //**
    // @method toBack
    // %result this
    // Moves the stage to the back of the window stack.
    private Object _toBack(Stage stage, Joe joe, Args args) {
        args.arity(0, "toBack()");
        stage.toBack();
        return stage;
    }

    //**
    // @method toFront
    // %result this
    // Moves the stage to the front of the window stack.
    private Object _toFront(Stage stage, Joe joe, Args args) {
        args.arity(0, "toFront()");
        stage.toFront();
        return stage;
    }
}
