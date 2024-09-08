package com.wjduquette.joe.types;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeList;
import com.wjduquette.joe.TypeProxy;

import java.util.List;

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

    private Object _init(Joe joe, List<Object> args) {
        return new ListValue(args);
    }

    //-------------------------------------------------------------------------
    // Method Implementation

    private Object _size(JoeList list, Joe joe, List<Object> args) {
        Joe.exactArity(args, 0, "size()");
        return (double)list.size();
    }

}
