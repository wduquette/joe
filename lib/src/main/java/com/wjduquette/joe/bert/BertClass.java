package com.wjduquette.joe.bert;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.JoeObject;

import java.util.HashMap;
import java.util.Map;

public class BertClass implements JoeObject {
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
    // JoeObject API

    @Override
    public String typeName() {
        return "<class>";
    }

    @Override
    public Object get(String name) {
        // TODO: Support static fields and methods
        throw new JoeError("Undefined property '" + name + "'.");
    }

    @Override
    public void set(String name, Object value) {
        // TODO: Support static fields
        throw new JoeError("Undefined property '" + name + "'.");
    }

    @Override
    public String stringify(Joe joe) {
        return "<class " + name + ">";
    }


    //-------------------------------------------------------------------------
    // Object API

    @Override
    public String toString() {
        return "<class " + name + ">";
    }
}
