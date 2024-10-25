package com.wjduquette.joe.types;

import com.wjduquette.joe.JoeClass;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.JoeObject;

import java.util.HashMap;
import java.util.Map;

public class TextBuilder implements JoeObject {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final JoeClass joeClass;
    private final Map<String,Object> fields = new HashMap<>();
    private StringBuilder buff = new StringBuilder();

    //-------------------------------------------------------------------------
    // Constructor

    public TextBuilder(JoeClass joeClass) {
        this.joeClass = joeClass;
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

    //-------------------------------------------------------------------------
    // JoeObject API

    @Override
    public String typeName() {
        return joeClass.name();
    }

    @Override
    public Object get(String name) {
        var value = fields.get(name);

        if (value == null) {
            value = joeClass.bind(this, name);
        }

        if (value != null) {
            return value;
        } else {
            throw new JoeError("Undefined property: '" + name + "'.");
        }
    }

    @Override
    public void set(String name, Object value) {
        fields.put(name, value);
    }

    @Override
    public String toString() {
        return buff.toString();
    }
}
