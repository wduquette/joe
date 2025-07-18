package com.wjduquette.joe.nero;

import java.util.List;
import java.util.Map;

/**
 * A Nero {@link Fact} type providing efficient named-field access
 * to a map of field names and values.
 * @param relation The relation
 * @param fieldMap The values.
 */
public record MapFact(String relation, Map<String, Object> fieldMap)
    implements Fact
{
    @Override public boolean             isOrdered()   { return false; }
    @Override public int                 arity()       { return fieldMap.size(); }
    @Override public Map<String, Object> getFieldMap() { return fieldMap; }

    @Override
    public List<Object> getFields() {
        throw new UnsupportedOperationException(
            "MapFact does not support positional field access.");
    }
}
