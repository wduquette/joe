package com.wjduquette.joe.bert;

import com.wjduquette.joe.*;
import com.wjduquette.joe.types.TypeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BertRecord is the internal representation for a scripted record type
 */
public class BertRecord
    implements JoeClass, JoeValue, NativeCallable, BertType
{
    //-------------------------------------------------------------------------
    // Instance Variables

    // The type name (and the name of its global variable)
    private final String name;

    // Static methods and constants
    final Map<String, Closure> staticMethods = new HashMap<>();
    private final Map<String, Object> fields = new HashMap<>();

    // Record field names
    private final String signature;
    private final List<String> recordFields;

    // The class's methods.
    final Map<String,Closure> methods = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor and building methods

    /**
     * Creates the class object.  It is initially empty, having only a name;
     * the compiled class declaration builds it up over a number of
     * instructions.
     * @param name The name
     * @param recordFields The record's field names.
     */
    BertRecord(String name, List<String> recordFields) {
        this.name = name;
        this.recordFields = recordFields;
        this.signature = name + "(" + String.join(", ", recordFields) + ")";
    }

    //-------------------------------------------------------------------------
    // BertRecord API

    public List<String> getRecordFields() {
        return recordFields;
    }

    public void addMethod(String name, Closure closure) {
        methods.put(name, closure);
    }

    public void addStaticMethod(String name, Closure closure) {
        staticMethods.put(name, closure);
    }

    public Object call(Joe joe, Args args) {
        args.exactArity(recordFields.size(), signature);
        var map = new HashMap<String,Object>();
        for (var i = 0; i < recordFields.size(); i++) {
            map.put(recordFields.get(i), args.get(i));
        }

        return new BertRecordValue(this, map);
    }


    //-------------------------------------------------------------------------
    // JoeType API

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isRecordType() {
        return true;
    }

    //-------------------------------------------------------------------------
    // JoeClass API

    @Override
    public JoeCallable bind(Object value, String name) {
        var method = methods.get(name);

        if (method != null) {
            return new BoundMethod(value, method);
        }

        return null;
    }

    @Override
    public boolean canBeExtended() {
        return true;
    }

    //-------------------------------------------------------------------------
    // JoeValue API -- This refers the type object itself and its statics!

    @Override
    public JoeType type() {
        return TypeType.TYPE;
    }

    @Override
    public String typeName() {
        return "<record>";
    }

    @Override
    public boolean hasField(String name) {
        return fields.containsKey(name);
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

    @Override
    public String stringify(Joe joe) {
        return "<class " + name + ">";
    }

    //-------------------------------------------------------------------------
    // JoeCallable API

    @Override
    public String callableType() {
        return "record";
    }

    @Override
    public boolean isScripted() {
        return true;
    }

    @Override
    public String signature() {
        return signature;
    }

    //-------------------------------------------------------------------------
    // Object API

    @Override
    public String toString() {
        return "<record " + name + ">";
    }
}
