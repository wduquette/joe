package com.wjduquette.joe;

import java.util.List;

public class JoeClass implements JoeCallable {
    private final String name;

    JoeClass(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public Object call(Interpreter interp, List<Object> args) {
        JoeInstance instance = new JoeInstance(this);
        return instance;
    }

    @Override
    public String toString() {
        return "<class " + name + ">";
    }
}
