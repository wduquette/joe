package com.wjduquette.joe.nero;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link Nero} {@link Fact} type providing efficient ordered-field access
 * to a list of field values.  Field names are `f0`, `f1`, `f2`, etc.
 * @param relation The relation
 * @param fields The values.
 */
public record ListFact(String relation, List<Object> fields)
    implements Fact
{
    @Override public boolean hasOrderedFields() { return true; }
    @Override public List<Object> getFields() { return fields; }

    @Override
    public Map<String, Object> getFieldMap() {
        // Create a field map when needed.
        var map = new LinkedHashMap<String, Object>();
        for (var i = 0; i < fields.size(); i++) {
            map.put("f" + i, fields.get(i));
        }
        return map;
    }
}
