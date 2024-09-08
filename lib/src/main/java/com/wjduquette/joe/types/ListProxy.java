package com.wjduquette.joe.types;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeList;
import com.wjduquette.joe.TypeProxy;

public class ListProxy extends TypeProxy<JoeList> {
    public static final ListProxy TYPE = new ListProxy();

    //-------------------------------------------------------------------------
    // Constructor

    public ListProxy() {
        super("List");
        proxies(JoeList.class);
        initializer(this::_init);
        method("size", this::_size);
    }

    //-------------------------------------------------------------------------
    // Initializer Implementation

    private Object _init(Joe joe, ArgQueue args) {
        return new ListValue(args.asList());
    }

    //-------------------------------------------------------------------------
    // Method Implementation

    private Object _size(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "size()");
        return (double)list.size();
    }

}
