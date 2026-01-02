package com.wjduquette.joe.walker;

import com.wjduquette.joe.*;
import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.NewFact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class WalkerRecord implements JoeValue {
    private final static String TO_STRING = "toString";

    //-------------------------------------------------------------------------
    // Instance Variables

    private final WalkerRecordType type;
    private final Map<String, Object> fieldMap;
    private List<Object> fields = null;

    // Default "toString()" implementation.
    private final NativeCallable _toString;

    //-------------------------------------------------------------------------
    // Constructor

    WalkerRecord(WalkerRecordType type, Map<String,Object> fieldMap) {
        this.type = type;
        this.fieldMap = fieldMap;
        this._toString = new NativeMethod<>(this, "toString",
            (objc, joe, args) -> toStringRep(joe));
    }

    private String toStringRep(Joe joe) {
        var values = type.getRecordFields().stream()
            .map(n -> joe.stringify(fieldMap.get(n)))
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
    public List<String> getFieldNames() {
        return type.getRecordFields();
    }

    @Override
    public Object get(String name) {
        if (fieldMap.containsKey(name)) {
            return fieldMap.get(name);
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
    public boolean hasMatchableFields() {
        return true;
    }

    @Override
    public Map<String,Object> getMatchableFieldMap() {
        return Collections.unmodifiableMap(fieldMap);
    }

    @Override
    public boolean hasOrderedMatchableFields() {
        return true;
    }

    @Override
    public List<Object> getMatchableFieldValues() {
        if (fields == null) {
            fields = new ArrayList<>();
            for (var name : type.getRecordFields()) {
                fields.add(fieldMap.get(name));
            }
        }
        return Collections.unmodifiableList(fields);
    }

    @Override
    public boolean isFact() {
        return !fieldMap.isEmpty();
    }

    @Override
    public Fact toFact() {
        return new NewFact(type.name(), type.getRecordFields(),
            getMatchableFieldValues());
    }

    @Override
    public String stringify(Joe joe) {
        var callable = get(TO_STRING);
        return (String)joe.call(callable);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        WalkerRecord that = (WalkerRecord) o;
        return type.equals(that.type) && fieldMap.equals(that.fieldMap);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + fieldMap.hashCode();
        return result;
    }

    @Override
    public String toString() {
        // This is for debugging only; it isn't used for the string rep.
        return "<" + type.name() + "@" + String.format("%x",hashCode()) + ">";
    }
}
