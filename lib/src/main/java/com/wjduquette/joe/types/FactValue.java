package com.wjduquette.joe.types;

import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.Nero;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The concrete Fact type for use with {@link Nero} in Joe scripts.
 * @param relation The relation
 * @param getFields The values.
 */
public record FactValue(String relation, List<Object> getFields)
    implements Fact
{
    @Override
    public boolean hasOrderedFields() {
        return true;
    }

    @Override
    public Map<String, Object> getFieldMap() {
        // Create a field map when needed.
        var map = new LinkedHashMap<String, Object>();
        for (var i = 0; i < getFields.size(); i++) {
            map.put("f" + i, getFields.get(i));
        }
        return map;
    }

    @Override public String toString() {
        return "Fact(" + relation + ", " + getFields + ")";
    }
}
