package com.wjduquette.joe.types;

import com.wjduquette.joe.*;

public class TextBuilder implements JoeObject {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The object infrastructure
    private final JoeObjectCore core;

    // The buffer
    private StringBuilder buff = new StringBuilder();

    //-------------------------------------------------------------------------
    // Constructor

    public TextBuilder(JoeClass joeClass) {
        this.core = new JoeObjectCore(joeClass, this);
    }

    //-------------------------------------------------------------------------
    // TextBuilder API

    public void clear() {
        this.buff = new StringBuilder();
    }

    public TextBuilder append(String value) {
        buff.append(value);
        return this;
    }

    public String toString() {
        return buff.toString();
    }

    //-------------------------------------------------------------------------
    // JoeObject API

    @Override public String typeName() { return core.typeName(); }
    @Override public Object get(String name) { return core.get(name); }
    @Override public void set(String name, Object value) { core.set(name, value); }
    @Override public String stringify(Joe joe) { return core.stringify(joe); }

}
