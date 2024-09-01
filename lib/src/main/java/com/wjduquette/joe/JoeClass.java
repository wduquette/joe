package com.wjduquette.joe;

public class JoeClass {
    private final String name;

    JoeClass(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "<class " + name + ">";
    }
}
