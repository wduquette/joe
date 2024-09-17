package com.wjduquette.joe;

import java.util.HashMap;
import java.util.Map;

class JoeInstance implements JoeObject {
    private final JoeClass joeClass;
    private final Map<String, Object> fields = new HashMap<>();

    JoeInstance(JoeClass klass) {
        this.joeClass = klass;
    }

    @Override
    public Object get(String name) {
        if (fields.containsKey(name)) {
            return fields.get(name);
        }

        JoeFunction method = joeClass.findMethod(name);
        if (method != null) return method.bind(this);

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
    public String toString() {
        return "<" + joeClass.name() + "@" + String.format("%x",hashCode()) + ">";
    }
}
