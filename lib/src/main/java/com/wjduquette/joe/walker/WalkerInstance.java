package com.wjduquette.joe.walker;

import com.wjduquette.joe.*;
import com.wjduquette.joe.types.ListValue;

import java.util.HashMap;
import java.util.Map;

class WalkerInstance implements JoeValue {
    private final static String TO_STRING = "toString";

    //-------------------------------------------------------------------------
    // Instance Variables

    private final JoeClass joeClass;
    private final Map<String, Object> fields = new HashMap<>();

    // Default "toString()" implementation.
    private final NativeCallable _toString;

    WalkerInstance(JoeClass joeClass) {
        this.joeClass = joeClass;
        this._toString = new NativeMethod<>(this, "toString",
            (objc, joe, args) -> this.toString());
    }

    //-------------------------------------------------------------------------
    // JoeValue API

    @Override
    public JoeType type() {
        return joeClass;
    }

    @Override
    public String typeName() {
        return joeClass.name();
    }

    @Override
    public boolean hasField(String name) {
        return fields.containsKey(name);
    }

    @Override
    public JoeList getFieldNames() {
        return new ListValue(fields.keySet());
    }

    @Override
    public Object get(String name) {
        if (fields.containsKey(name)) {
            return fields.get(name);
        }

        JoeCallable method = joeClass.bind(this, name);

        if (method != null) return method;

        if (name.equals(TO_STRING)) {
            return _toString;
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
