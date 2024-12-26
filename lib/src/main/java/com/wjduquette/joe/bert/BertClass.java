package com.wjduquette.joe.bert;

public class BertClass {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final String name;

    //-------------------------------------------------------------------------
    // Constructor

    BertClass(String name) {
        this.name = name;
    }

    //-------------------------------------------------------------------------
    // BertClass API

    public String name() {
        return name;
    }

    //-------------------------------------------------------------------------
    // Object API

    @Override
    public String toString() {
        return "<class " + name + ">";
    }
}
