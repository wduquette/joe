package com.wjduquette.joe.bert;

import java.util.HashMap;
import java.util.Map;

public class BertClass {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The class name (and the name of its global variable)
    private final String name;

    // The class's methods.
    final Map<String,Closure> methods = new HashMap<>();

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
