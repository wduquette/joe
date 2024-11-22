package com.wjduquette.joe;

import java.util.HashMap;
import java.util.Map;

public class JoeObjectCore {
    private final static String TO_STRING = "toString";

    //-------------------------------------------------------------------------
    // Instance Variables

    private final JoeClass joeClass;

    private final Object host;
    private final Map<String,Object> fields = new HashMap<>();

    // Default "toString()" implementation.
    private final JoeCallable _toString;

    //-------------------------------------------------------------------------
    // Constructor

    public JoeObjectCore(JoeClass joeClass, Object host) {
        this.joeClass = joeClass;
        this.host = host;
        this._toString = new NativeMethod<>(this, "toString",
            (objc, joe, args) -> this.defaultToString());
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

        if (value != null) return value;

        if (name.equals(TO_STRING)) {
            return _toString;
        }

        throw new JoeError("Undefined property: '" + name + "'.");
    }

    public void set(String name, Object value) {
        fields.put(name, value);
    }

    public String stringify(Joe joe) {
        var callable = get(TO_STRING);
        return (String)joe.call(callable);
    }

    public String defaultToString() {
        return typeName() + "@" + host.hashCode();
    }
}
