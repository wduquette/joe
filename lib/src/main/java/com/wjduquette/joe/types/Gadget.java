package com.wjduquette.joe.types;

import com.wjduquette.joe.JoeClass;
import com.wjduquette.joe.JoeObject;

import java.util.HashMap;
import java.util.Map;

public class Gadget implements JoeObject {
    private final JoeClass joeClass;
    private final Map<String,Object> fields = new HashMap<>();

    public Gadget(JoeClass joeClass) {
        this.joeClass = joeClass;
    }

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

        return value;
    }

    @Override
    public void set(String name, Object value) {
        fields.put(name, value);
    }
}
