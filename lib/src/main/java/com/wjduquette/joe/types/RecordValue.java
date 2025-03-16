package com.wjduquette.joe.types;

import com.wjduquette.joe.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A class representing an immutable record, similar to a Java record.
 */
public class RecordValue implements JoeRecord {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final RecordType<? extends RecordValue> type;
    private final Map<String,Object> fields = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    public RecordValue(
        RecordType<? extends RecordValue> type,
        Object... fieldValues
    ) {
        this.type = type;

        var names = type.getRecordFields();
        for (int i = 0; i < names.size(); i++) {
            fields.put(names.get(i), fieldValues[i]);
        }
    }

    //-------------------------------------------------------------------------
    // JoeValue API

    @Override
    public JoeType type() {
        return type;
    }

    @Override
    public String typeName() {
        return type.name();
    }

    @Override
    public boolean hasField(String name) {
        return false;
    }

    @Override
    public List<String> getFieldNames() {
        return type.getRecordFields();
    }

    @Override
    public Object get(String name) {
        var method = type.bind(this, name);

        if (method != null) {
            return method;
        } if (fields.containsKey(name)) {
            return fields.get(name);
        } else {
            throw new JoeError("Unknown property: '" + name + "'.");
        }
    }

    @Override
    public void set(String name, Object value) {
        throw new JoeError("Record types are immutable.");
    }

    @Override
    public String stringify(Joe joe) {
        var values = type.getRecordFields().stream()
            .map(name -> joe.stringify(fields.get(name)))
            .collect(Collectors.joining(", "));
        return type.name() + "(" + values + ")";
    }
}
