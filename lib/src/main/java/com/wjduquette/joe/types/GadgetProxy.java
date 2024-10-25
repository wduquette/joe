package com.wjduquette.joe.types;

import com.wjduquette.joe.*;

public class GadgetProxy extends TypeProxy<Gadget> {
    /** The type constant, for installation. */
    public static final GadgetProxy TYPE = new GadgetProxy();

    //-------------------------------------------------------------------------
    // Constructor

    /** Creates the proxy. */
    public GadgetProxy() {
        super("Gadget");

        proxies(Gadget.class);

        initializer(this::_init);

        method("howdy",  this::_howdy);
        method("toString", this::_toString);
    }

    //-------------------------------------------------------------------------
    // JoeClass API

    @Override
    public boolean canBeExtended() {
        return true;
    }

    @Override
    public JoeObject make(JoeClass joeClass) {
        return new Gadget(joeClass);
    }

    //-------------------------------------------------------------------------
    // Initializer

    private Object _init(Joe joe, Args args) {
        return make(this);
    }

    //-------------------------------------------------------------------------
    // Method Implementations

    private Object _howdy(Gadget value, Joe joe, Args args) {
        Joe.exactArity(args, 0, "howdy()");
        return "Howdy!";
    }

    private Object _toString(Gadget value, Joe joe, Args args) {
        Joe.exactArity(args, 0, "toString");
        return joe.stringify(value);
    }
}
