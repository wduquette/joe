package com.wjduquette.joe.clark;

import com.wjduquette.joe.*;
import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.MapFact;

import java.util.*;

/**
 * BertInstance is the internal representation for instances of scripted
 * classes—unless the scripted class has a native ancestor, in which
 * case instances are proved by the native ancestor.
 */
public class ClarkInstance implements JoeValue {
    private final static String TO_STRING = "toString";

    //-------------------------------------------------------------------------
    // Instance Variables

    // The object's class, which will de facto be a ClarkClass.
    final JoeClass klass;

    // The object's field values.
    final Map<String,Object> fields = new HashMap<>();

    // Default "toString()" implementation.
    private final NativeCallable _toString;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the internal representation for an instance of a scripted
     * class.
     * @param klass The class.
     */
    ClarkInstance(JoeClass klass) {
        this.klass = klass;

        // The class might define its own explicit toString() method;
        // but provide a default.
        this._toString = new NativeMethod<>(this, "toString",
            (objc, joe, args) -> this.toString());
    }

    //-------------------------------------------------------------------------
    // JoeValue API

    @Override
    public JoeType type() {
        return klass;
    }

    @Override
    public List<String> getFieldNames() {
        return new ArrayList<>(fields.keySet());
    }

    @Override
    public boolean hasField(String name) {
        return fields.containsKey(name);
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
    public boolean isFact() {
        return !fields.isEmpty();
    }

    @Override
    public Fact toFact() {
        return new MapFact(klass.name(), fields);
    }

    @Override
    public String stringify(Joe joe) {
        var callable = get(TO_STRING);
        return (String)joe.call(callable);
    }

    //-------------------------------------------------------------------------
    // Fact API

    @Override
    public Map<String,Object> getFieldMap() {
        return Collections.unmodifiableMap(fields);
    }

    //-------------------------------------------------------------------------
    // Object API

    @Override
    public String toString() {
        return "<" + klass.name() + "@" + String.format("%x",hashCode()) + ">";
    }
}
