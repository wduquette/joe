package com.wjduquette.joe.win;

import com.wjduquette.joe.JoeClass;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.JoeObject;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class JoeVBox extends VBox implements JoeObject {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final JoeClass joeClass;
    private final Map<String,Object> fields = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    public JoeVBox(JoeClass joeClass) {
        this.joeClass = joeClass;
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
}
