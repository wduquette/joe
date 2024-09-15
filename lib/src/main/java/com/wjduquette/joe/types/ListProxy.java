package com.wjduquette.joe.types;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeList;
import com.wjduquette.joe.TypeProxy;

public class ListProxy extends TypeProxy<JoeList> {
    public static final ListProxy TYPE = new ListProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe
    // @type List
    // A Joe `List` is a Java `List`, roughly equivalent to a Java `ArrayList`.
    // Lists created using the [[List#init]] initializer can contain any kind
    // of Joe value; the list need not be homogeneous.  Lists received from
    // Java code might be read-only or require a specific item type.
    public ListProxy() {
        super("List");
        proxies(JoeList.class);
        initializer(this::_init);
        method("size", this::_size);
    }

    //-------------------------------------------------------------------------
    // Initializer Implementation

    //**
    // @init
    // @args values...
    // Creates a `List` of the argument values.
    private Object _init(Joe joe, ArgQueue args) {
        return new ListValue(args.asList());
    }

    //-------------------------------------------------------------------------
    // Method Implementation

    //**
    // @method size
    // @result Number
    // Returns the number of items in the list.
    private Object _size(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "size()");
        return (double)list.size();
    }

}
