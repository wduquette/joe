package com.wjduquette.joe.types;

import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.Nero;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The concrete Fact type for use with {@link Nero} in Joe scripts.
 * @param relation The relation
 * @param fields The values.
 */
public record FactValue(String relation, List<Object> fields)
    implements Fact
{
    @Override
    public boolean hasOrderedFields() {
        return true;
    }

    @Override
    public Map<String, Object> fieldMap() {
        // Create a field map when needed.
        var map = new LinkedHashMap<String, Object>();
        for (var i = 0; i < fields.size(); i++) {
            map.put("f" + i, fields.get(i));
        }
        return map;
    }

    @Override public String toString() {
        return "Fact(" + relation + ", " + fields + ")";
    }
}
