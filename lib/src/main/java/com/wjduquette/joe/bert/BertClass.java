package com.wjduquette.joe.bert;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.JoeObject;

import java.util.HashMap;
import java.util.Map;

public class BertClass implements BertCallable, JoeObject {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The class name (and the name of its global variable)
    private final String name;

    // Static methods and constants
    final Map<String, Closure> staticMethods = new HashMap<>();
    private final Map<String, Object> fields = new HashMap<>();

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
        if (fields.containsKey(name)) {
            return fields.get(name);
        }

        if (staticMethods.containsKey(name)) {
            return staticMethods.get(name);
        }

        throw new JoeError("Undefined property '" + name + "'.");
    }

    @Override
    public void set(String name, Object value) {
        fields.put(name, value);
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
