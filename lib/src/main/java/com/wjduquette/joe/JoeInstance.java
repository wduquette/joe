package com.wjduquette.joe;

import java.util.HashMap;
import java.util.Map;

class JoeInstance implements JoeObject {
    private final JoeClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    JoeInstance(JoeClass klass) {
        this.klass = klass;
    }

    @Override
    public Object get(String name) {
        if (fields.containsKey(name)) {
            return fields.get(name);
        }

        JoeFunction method = klass.findMethod(name);
        if (method != null) return method.bind(this);

        throw new JoeError("Undefined property '" + name + "'.");
    }

    @Override
    public void set(String name, Object value) {
        fields.put(name, value);
    }

    @Override
    public String toString() {
        return "<" + klass.name() + "@" + String.format("%x",hashCode()) + ">";
    }
}
