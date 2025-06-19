package com.wjduquette.joe.walker;

import com.wjduquette.joe.*;
import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.ListFact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class WalkerRecordValue implements JoeValue {
    private final static String TO_STRING = "toString";

    //-------------------------------------------------------------------------
    // Instance Variables

    private final WalkerRecord type;
    private final Map<String, Object> fields;

    // Default "toString()" implementation.
    private final NativeCallable _toString;

    //-------------------------------------------------------------------------
    // Constructor

    WalkerRecordValue(WalkerRecord type, Map<String,Object> fields) {
        this.type = type;
        this.fields = fields;
        this._toString = new NativeMethod<>(this, "toString",
            (objc, joe, args) -> toStringRep(joe));
    }

    private String toStringRep(Joe joe) {
        var values = type.getRecordFields().stream()
            .map(n -> joe.stringify(fields.get(n)))
            .collect(Collectors.joining(", "));
        return type.name() + "(" + values + ")";
    }

    //-------------------------------------------------------------------------
    // JoeValue API

    @Override
    public JoeType type() {
        return type;
    }

    @Override
    public boolean hasField(String name) {
        return fields.containsKey(name);
    }

    @Override
    public List<String> getFieldNames() {
        return type.getRecordFields();
    }

    @Override
    public Object get(String name) {
        if (fields.containsKey(name)) {
            return fields.get(name);
        }

        JoeCallable method = type.bind(this, name);

        if (method != null) return method;

        if (name.equals(TO_STRING)) {
            return _toString;
        }

        throw new JoeError("Undefined property '" + name + "'.");
    }

    @Override
    public void set(String name, Object value) {
        throw new JoeError("Values of type " + type.name() +
            " have no mutable properties.");
    }

    @Override
    public boolean isFact() {
        return !fields.isEmpty();
    }

    @Override
    public Fact toFact() {
        return new ListFact(type.name(), getFields());
    }

    @Override
    public String stringify(Joe joe) {
        var callable = get(TO_STRING);
        return (String)joe.call(callable);
    }

    @Override
    public String toString() {
        // This is for debugging only; it isn't used for the string rep.
        return "<" + type.name() + "@" + String.format("%x",hashCode()) + ">";
    }

    //-------------------------------------------------------------------------
    // Fact API

    /**
     * Every record instance has ordered fields.
     * @return true
     */
    @Override
    public final boolean hasOrderedFields() {
        return true;
    }

    /**
     * Gets the values of the record's fields, in order.
     * @return The values
     */
    @Override
    public final List<Object> getFields() {
        var list = new ArrayList<>();

        for (var name : type.getRecordFields()) {
            list.add(get(name));
        }

        return list;
    }

    @Override
    public Map<String, Object> getFieldMap() {
        return Collections.unmodifiableMap(fields);
    }
}
