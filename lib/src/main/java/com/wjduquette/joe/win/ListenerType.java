package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.ProxyType;

/**
 * Proxy for a JavaFX Button.
 */
class ListenerType extends ProxyType<Listener> {
    public static final ListenerType TYPE = new ListenerType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type Listener
    // Various methods that add a listener to a list of listeners return
    // `Listener` objects.  Use the listener's `cancel()` method to
    // remove the listener from the list and stop listening.
    public ListenerType() {
        super("Listener");
        proxies(Listener.class);

        // Methods
        method("cancel",   this::_cancel);
        method("toString", this::_toString);
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method cancel
    // Cancels the listener; the relevant callback will no longer be
    // called.
    private Object _cancel(Listener listener, Joe joe, Args args) {
        args.exactArity(0, "cancel()");
        listener.cancel();
        return null;
    }

    //**
    // @method toString
    // %result joe.String
    // Gets the listener's string representation.
    private Object _toString(Listener listener, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return listener.toString();
    }
}
