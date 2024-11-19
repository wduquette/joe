package com.wjduquette.joe;

import java.util.HashMap;
import java.util.Map;

public class JoeObjectCore {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final JoeClass joeClass;
    private final Object host;
    private final Map<String,Object> fields = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    public JoeObjectCore(JoeClass joeClass, Object host) {
        this.joeClass = joeClass;
        this.host = host;
    }

    //-------------------------------------------------------------------------
    // Object Method implementations

    public String typeName() {
        return joeClass.name();
    }

    public Object get(String name) {
        var value = fields.get(name);

        if (value == null) {
            value = joeClass.bind(host, name);
        }

        if (value != null) {
            return value;
        } else {
            throw new JoeError("Undefined property: '" + name + "'.");
        }
    }

    public void set(String name, Object value) {
        fields.put(name, value);
    }

    public String stringify(Joe joe) {
        // TEMP
        return host.toString();
    }
}
