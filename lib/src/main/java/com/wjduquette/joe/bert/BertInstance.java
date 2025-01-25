package com.wjduquette.joe.bert;

import com.wjduquette.joe.*;

import java.util.HashMap;
import java.util.Map;

public class BertInstance implements JoeObject {
    private final static String TO_STRING = "toString";

    //-------------------------------------------------------------------------
    // Instance Variables

    final JoeClass klass;
    final Map<String,Object> fields = new HashMap<>();

    // Default "toString()" implementation.
    private final NativeCallable _toString;

    //-------------------------------------------------------------------------
    // Constructor

    BertInstance(JoeClass klass) {
        this.klass = klass;
        this._toString = new NativeMethod<>(this, "toString",
            (objc, joe, args) -> this.toString());
    }

    //-------------------------------------------------------------------------
    // JoeObject API

    @Override
    public String typeName() {
        return klass.name();
    }

    @Override
    public Object get(String name) {
        if (fields.containsKey(name)) {
            return fields.get(name);
        }

        var method = klass.bind(this, name);

        if (method != null) {
            return method;
        }

        if (name.equals(TO_STRING)) {
            return _toString;
        }

        throw new JoeError("Undefined property '" + name + "'.");
    }

    @Override
    public void set(String name, Object value) {
        fields.put(name, value);
    }

    @Override
    public String stringify(Joe joe) {
        var callable = get(TO_STRING);
        return (String)joe.call(callable);
    }


    //-------------------------------------------------------------------------
    // Object API

    @Override
    public String toString() {
        return "<" + klass.name() + "@" + String.format("%x",hashCode()) + ">";
    }
}
