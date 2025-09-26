package com.wjduquette.joe;

import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.MapFact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Instance implements JoeValue {
    private final static String TO_STRING = "toString";

    //-------------------------------------------------------------------------
    // Instance Variables

    // The class for which this is an instance
    private final JoeClass joeClass;

    // The instance's own fields
    private final Map<String,Object> fieldMap;

    //-------------------------------------------------------------------------
    // Constructor

    public Instance(JoeClass joeClass, Map<String,Object> fieldMap) {
        this.joeClass = joeClass;
        this.fieldMap = fieldMap;
    }

    //-------------------------------------------------------------------------
    // JoeValue API

    @Override
    public JoeType type() {
        return joeClass;
    }

    @Override
    public List<String> getFieldNames() {
        return new ArrayList<>(fieldMap.keySet());
    }

    @Override
    public Object get(String name) {
        if (fieldMap.containsKey(name)) {
            return fieldMap.get(name);
        }

        JoeCallable method = joeClass.bind(this, name);

        if (method != null) return method;

        throw new JoeError("Undefined property '" + name + "'.");
    }

    @Override
    public void set(String name, Object value) {
        fieldMap.put(name, value);
    }

    @Override
    public String stringify(Joe joe) {
        var callable = get(TO_STRING);
        return (String)joe.call(callable);
    }

    @Override
    public boolean hasMatchableFields() {
        return !fieldMap.isEmpty();
    }

    @Override
    public Map<String,Object> getMatchableFieldMap() {
        return Collections.unmodifiableMap(fieldMap);
    }

    @Override
    public boolean isFact() {
        return !fieldMap.isEmpty();
    }

    @Override
    public Fact toFact() {
        return new MapFact(joeClass.name(), fieldMap);
    }

    @Override
    public String toString() {
        return "<" + joeClass.name() + "@" + String.format("%x",hashCode()) + ">";
    }
}
