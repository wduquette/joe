package com.wjduquette.joe.walker;

import com.wjduquette.joe.*;

import java.util.HashMap;
import java.util.Map;

class WalkerInstance implements JoeObject {
    private final static String TO_STRING = "toString";
    private final JoeClass joeClass;
    private final Map<String, Object> fields = new HashMap<>();

    WalkerInstance(JoeClass joeClass) {
        this.joeClass = joeClass;
    }

    @Override
    public String typeName() {
        return joeClass.name();
    }

    @Override
    public Object get(String name) {
        if (fields.containsKey(name)) {
            return fields.get(name);
        }

        JoeCallable method = joeClass.bind(this, name);
        if (method != null) return method;

        if (name.equals(TO_STRING)) {
            return (JoeCallable)(joe, args) -> this.toString();
        }

        throw new JoeError("Undefined property '" + name + "'.");
    }

    @Override
    public void set(String name, Object value) {
        fields.put(name, value);
    }

    public JoeClass joeClass() {
        return joeClass;
    }

    @Override
    public String stringify(Joe joe) {
        var callable = get(TO_STRING);
        return (String)joe.call(callable);
    }

    @Override
    public String toString() {
        return "<" + joeClass.name() + "@" + String.format("%x",hashCode()) + ">";
    }
}
