package com.wjduquette.joe;

public class NativeFunction implements JoeCallable {
    private final String name;
    private final JoeCallable callable;

    public NativeFunction(String name, JoeCallable callable) {
        this.name = name;
        this.callable = callable;
    }

    public String name() {
        return name;
    }

    @Override
    public Object call(Joe joe, ArgQueue args) {
        return callable.call(joe, args);
    }

    @Override
    public String toString() {
        return "<native fn " + name + ">";
    }
}
