package com.wjduquette.joe;

import java.util.List;

public class NativeFunction implements JoeCallable {
    private final String name;
    private final JoeCallable callable;

    public NativeFunction(String name, JoeCallable callable) {
        this.name = name;
        this.callable = callable;
    }

    @Override
    public Object call(Interpreter interp, List<Object> args) {
        return callable.call(interp, args);
    }

    @Override
    public String toString() {
        return "<native fn " + name + ">";
    }
}
