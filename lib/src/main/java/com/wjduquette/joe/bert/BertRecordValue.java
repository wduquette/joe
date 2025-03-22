package com.wjduquette.joe.bert;

import com.wjduquette.joe.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BertRecordValue is the internal representation for instances of scripted
 * record types.
 */
public class BertRecordValue implements JoeValue {
    private final static String TO_STRING = "toString";

    //-------------------------------------------------------------------------
    // Instance Variables

    // The object's type
    final BertRecord type;

    // The object's field values.
    final Map<String,Object> fields;

    // Default "toString()" implementation.
    private final NativeCallable _toString;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the internal representation for an instance of a scripted
     * class.
     * @param type The class.
     * @param fields The field values.
     */
    BertRecordValue(BertRecord type, Map<String,Object> fields) {
        this.type = type;
        this.fields = fields;

        // The class might define its own explicit toString() method;
        // but provide a default.
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

        var method = type.bind(this, name);

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
        throw new JoeError("Values of type " + type.name() +
            " have no mutable properties.");
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
        return "<" + type.name() + "@" + String.format("%x",hashCode()) + ">";
    }
}
