package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.stage.Window;

/**
 * Proxy type for the JavaFX Window widget.
 */
public class WindowType extends WidgetType<Window> {
    /** Proxy type for installation into an interpreter. */
    public static final WindowType TYPE = new WindowType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @widget Window
    // %javaType javafx.stage.Window
    // %proxyType com.wjduquette.joe.win.WindowType
    // The `Window` type is the abstract base class for all JavaFX
    // windows, including [[Stage]].
    /** Constructor. */
    public WindowType() {
        super("Window");
        proxies(Window.class);

        // Properties

        //**
        // @property focused joe.Boolean
        // (Read-only) Does the window have the input focus?
        fxReadOnly("focused", Window::focusedProperty);

        //**
        // @property height joe.Number
        // (Read-only) Current height in pixels.
        fxReadOnly("height", Window::heightProperty);

        //**
        // @property onCloseRequest callable/1
        // See [[method:Window.onCloseRequest]].
        fxEvent("onCloseRequest", Window::onCloseRequestProperty);

        //**
        // @property onHidden callable/1
        // See [[method:Window.onHidden]].
        fxEvent("onHidden", Window::onHiddenProperty);

        //**
        // @property onHiding callable/1
        // See [[method:Window.onHiding]].
        fxEvent("onHiding", Window::onHidingProperty);

        //**
        // @property onShowing callable/1
        // See [[method:Window.onShowing]].
        fxEvent("onShowing", Window::onShowingProperty);

        //**
        // @property onShown callable/1
        // See [[method:Window.onShown]].
        fxEvent("onShown", Window::onShownProperty);

        //**
        // @property showing joe.Boolean
        // (Read-only) Is the window open on the screen?
        fxReadOnly("showing", Window::showingProperty);

        //**
        // @property width joe.Number
        // (Read-only) Current width in pixels
        fxReadOnly("width", Window::widthProperty);

        //**
        // @property x joe.Number
        // (Read-only) Horizontal location of this window on the screen.
        fxReadOnly("x", Window::xProperty);

        //**
        // @property y joe.Number
        // (Read-only) Vertical location of this window on the screen.
        fxReadOnly("y", Window::yProperty);

        // No initializer

        // Methods
        method("centerOnScreen", this::_centerOnScreen);
        method("getHeight",      this::_getHeight);
        method("getWidth",       this::_getWidth);
        method("getScene",       this::_getScene);
        method("getUserData",    this::_getUserData);
        method("hide",           this::_hide);
        method("isFocused",      this::_isFocused);
        method("isShowing",      this::_isShowing);
        method("height",         this::_height);
        method("onCloseRequest", this::_onCloseRequest);
        method("onHidden",       this::_onHidden);
        method("onHiding",       this::_onHiding);
        method("onShowing",      this::_onShowing);
        method("onShown",        this::_onShown);
        method("requestFocus",   this::_requestFocus);
        method("sizeToScene",    this::_sizeToScene);
        method("userData",       this::_userData);
        method("width",          this::_width);
        method("x",              this::_x);
        method("y",              this::_y);

    }


    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method centerOnScreen
    // %result this
    // Centers the window on the screen.
    private Object _centerOnScreen(Window win, Joe joe, Args args) {
        args.arity(0, "centerOnScreen()");
        win.centerOnScreen();
        return win;
    }

    //**
    // @method getHeight
    // %result joe.Number
    // Gets the window's `#height` in pixels.
    // See [[property:Window.height]].
    private Object _getHeight(Window win, Joe joe, Args args) {
        args.arity(0, "getHeight()");
        return win.getHeight();
    }

    //**
    // @method getScene
    // %result Scene
    // Gets the scene displayed in the window.
    private Object _getScene(Window win, Joe joe, Args args) {
        args.arity(0, "getScene()");
        return win.getScene();
    }

    //**
    // @method getUserData
    // %result value
    // Gets the window's `userData` value.
    private Object _getUserData(Window win, Joe joe, Args args) {
        args.arity(0, "getUserData()");
        return win.getUserData();
    }

    //**
    // @method getWidth
    // %result joe.Number
    // Gets the window's `#width` in pixels.
    // See [[property:Window.width]].
    private Object _getWidth(Window win, Joe joe, Args args) {
        args.arity(0, "getWidth()");
        return win.getWidth();
    }

    //**
    // @method height
    // %args pixels
    // %result this
    // Sets the height of the widget in pixels.
    private Object _height(Window win, Joe joe, Args args) {
        args.arity(1, "height(pixels)");
        win.setHeight(joe.toDouble(args.next()));
        return win;
    }

    //**
    // @method hide
    // %result this
    // Pops the window down.
    private Object _hide(Window win, Joe joe, Args args) {
        args.arity(0, "hide()");
        win.hide();
        return win;
    }

    //**
    // @method isFocused
    // %result joe.Boolean
    // Gets whether the window has the input focus or not.
    // See [[property:Window.focused]].
    private Object _isFocused(Window win, Joe joe, Args args) {
        args.arity(0, "isFocused()");
        return win.isFocused();
    }

    //**
    // @method isShowing
    // %result joe.Boolean
    // Gets whether the window is showing on the screen.
    // See [[property:Window.showing]].
    private Object _isShowing(Window win, Joe joe, Args args) {
        args.arity(0, "isShowing()");
        return win.isShowing();
    }

    //**
    // @method onCloseRequest
    // %args callable
    // %result this
    // Adds a *callable/1* to the window as its
    // [[property:Window.onCloseRequest]]` handler.  The callable is called
    // when the user explicitly closes the window, and can prevent the window
    // from closing by disposing of the event.
    private Object _onCloseRequest(Window win, Joe joe, Args args) {
        args.arity(1, "onCloseRequest(callable)");
        win.setOnCloseRequest(Win.toWindowEvent(joe, args.next()));
        return win;
    }

    //**
    // @method onHidden
    // %args callable
    // %result this
    // Adds a *callable/1* to the window as its
    // [[property:Window.onHidden]]` handler.  The callable is called
    // after the window is removed from the screen.
    private Object _onHidden(Window win, Joe joe, Args args) {
        args.arity(1, "onHidden(callable)");
        win.setOnHidden(Win.toWindowEvent(joe, args.next()));
        return win;
    }


    //**
    // @method onHiding
    // %args callable
    // %result this
    // Adds a *callable/1* to the window as its
    // [[property:Window.onHiding]]` handler.  The callable is called
    // just before the window is removed from the screen.
    private Object _onHiding(Window win, Joe joe, Args args) {
        args.arity(1, "onHiding(callable)");
        win.setOnHiding(Win.toWindowEvent(joe, args.next()));
        return win;
    }

    //**
    // @method onShowing
    // %args callable
    // %result this
    // Adds a *callable/1* to the window as its
    // [[property:Window.onShowing]]` handler.  The callable is called
    // just before the window is displayed on the screen.
    private Object _onShowing(Window win, Joe joe, Args args) {
        args.arity(1, "onShowing(callable)");
        win.setOnShowing(Win.toWindowEvent(joe, args.next()));
        return win;
    }


    //**
    // @method onShown
    // %args callable
    // %result this
    // Adds a *callable/1* to the window as its
    // [[property:Window.onShown]]` handler.  The callable is called
    // just after the window is displayed on the screen.
    private Object _onShown(Window win, Joe joe, Args args) {
        args.arity(1, "onShown(callable)");
        win.setOnShown(Win.toWindowEvent(joe, args.next()));
        return win;
    }

    //**
    // @method requestFocus
    // %result this
    // Requests the input focus for this window.
    private Object _requestFocus(Window win, Joe joe, Args args) {
        args.arity(0, "requestFocus()");
        win.requestFocus();
        return win;
    }

    //**
    // @method sizeToScene
    // %result this
    // Sizes the window to fit the scene.
    private Object _sizeToScene(Window win, Joe joe, Args args) {
        args.arity(0, "sizeToScene()");
        win.sizeToScene();
        return win;
    }

    //**
    // @method userData
    // %args value
    // %result this
    // Sets the widget's `userData` property.
    private Object _userData(Window win, Joe joe, Args args) {
        args.arity(1, "userData(value)");
        win.setUserData(args.next());
        return win;
    }

    //**
    // @method width
    // %args pixels
    // %result this
    // Sets the width of the widget in pixels.
    private Object _width(Window win, Joe joe, Args args) {
        args.arity(1, "width(pixels)");
        win.setWidth(joe.toDouble(args.next()));
        return win;
    }

    //**
    // @method x
    // %args pixels
    // %result this
    // Sets the horizontal position of the window on the screen.
    private Object _x(Window win, Joe joe, Args args) {
        args.arity(1, "x(pixels)");
        win.setX(joe.toDouble(args.next()));
        return win;
    }

    //**
    // @method y
    // %args pixels
    // %result this
    // Sets the vertical position of the window on the screen.
    private Object _y(Window win, Joe joe, Args args) {
        args.arity(1, "y(pixels)");
        win.setY(joe.toDouble(args.next()));
        return win;
    }
}
