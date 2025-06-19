package com.wjduquette.joe.walker;

import com.wjduquette.joe.*;
import com.wjduquette.joe.SourceBuffer.Span;
import com.wjduquette.joe.types.TypeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A record type defined in a Joe script.
 */
class WalkerRecord implements JoeClass, JoeValue, NativeCallable {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The type's name
    private final String name;

    // The type's span in the source code.
    private final Span typeSpan;

    // Static methods and constants
    private final Map<String, WalkerFunction> staticMethods;
    private final Map<String, Object> fields = new HashMap<>();

    // Record field names
    private final String signature;
    private final List<String> recordFields;

    // Instance Methods
    private final Map<String, WalkerFunction> methods;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new record type
     * @param name The type's variable name.
     * @param typeSpan The type's span in the source code
     * @param recordFields The record field names
     * @param staticMethods The map of static methods by name
     * @param methods The map of methods by name
     */
    WalkerRecord(
        String name,
        Span typeSpan,
        List<String> recordFields,
        Map<String, WalkerFunction> staticMethods,
        Map<String, WalkerFunction> methods
    ) {
        this.name = name;
        this.typeSpan = typeSpan;
        this.recordFields = recordFields;
        this.staticMethods = staticMethods;
        this.methods = methods;
        this.signature = name + "(" + String.join(", ", recordFields) + ")";
    }

    //-------------------------------------------------------------------------
    // WalkerClass API

    @SuppressWarnings("unused")
    public Span typeSpan() {
        return typeSpan;
    }

    public Object call(Joe joe, Args args) {
        args.exactArity(recordFields.size(), signature);
        var map = new HashMap<String,Object>();
        for (var i = 0; i < recordFields.size(); i++) {
            map.put(recordFields.get(i), args.get(i));
        }

        return new WalkerRecordValue(this, map);
    }

    public List<String> getRecordFields() {
        return recordFields;
    }

    //-------------------------------------------------------------------------
    // JoeType API

    @Override
    public String name() {
        return name;
    }

    //-------------------------------------------------------------------------
    // JoeClass API

    @Override
    public JoeCallable bind(Object value, String name) {
        var method = methods.get(name);

        if (method != null) {
            return method.bind((JoeValue)value);
        }

        return null;
    }

    //-------------------------------------------------------------------------
    // JoeCallable API

    @Override
    public String callableType() {
        return "record";
    }

    @Override
    public String signature() {
        return signature;
    }

    @Override
    public boolean isScripted() {
        return true;
    }

    //-------------------------------------------------------------------------
    // JoeValue API

    @Override
    public JoeType type() {
        return TypeType.TYPE;
    }

    @Override
    public List<String> getFieldNames() {
        return new ArrayList<>(fields.keySet());
    }

    @Override
    public Object get(String name) {
        if (fields.containsKey(name)) {
            return fields.get(name);
        }

        if (staticMethods.containsKey(name)) {
            return staticMethods.get(name);
        }

        throw new JoeError("Undefined property '" + name + "'.");
    }

    @Override
    public void set(String name, Object value) {
        fields.put(name, value);
    }

    //-------------------------------------------------------------------------
    // Object API

    @Override
    public String toString() {
        return "<class " + name + ">";
    }
}
