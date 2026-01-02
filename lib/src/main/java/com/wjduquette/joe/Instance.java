package com.wjduquette.joe;

import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.ConcreteFact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The Java representation for instances of Joe classes, including native
 * classes.  For scripted classes, an instance simply is an instance of
 * this class.  For native classes extensible by scripts, an instance is
 * an instance of the native class and of JoeInstance, and is wrapped as
 * an Instance as needed.
 */
public class Instance implements JoeValue {
    private final static String TO_STRING = "toString";

    //-------------------------------------------------------------------------
    // Instance Variables

    // The actual instance
    private final Object self;

    // The class for which this is an instance
    private final JoeClass joeClass;

    // The instance's own fields
    private final Map<String,Object> fieldMap;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an instance given the Joe class and field map.
     * @param joeClass The class
     * @param fieldMap The field map
     */
    public Instance(JoeClass joeClass, Map<String,Object> fieldMap) {
        this.self = this;
        this.joeClass = joeClass;
        this.fieldMap = fieldMap;
    }

    /**
     * Creates an instance that wraps a NativeInstance.
     * @param joeInstance The NativeInstance
     */
    public Instance(JoeInstance joeInstance) {
        this.self = joeInstance;
        this.joeClass = joeInstance.getJoeClass();
        this.fieldMap = joeInstance.getInstanceFieldMap();
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

        JoeCallable method = joeClass.bind(self, name);

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
        return new ConcreteFact(joeClass.name(), fieldMap);
    }

    @Override
    public String toString() {
        return "<" + joeClass.name() + "@" + String.format("%x",hashCode()) + ">";
    }
}
