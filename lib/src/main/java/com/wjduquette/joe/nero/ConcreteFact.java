package com.wjduquette.joe.nero;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The default concrete Fact type for use with {@link Nero}.
 * @param relation The relation
 * @param getFields The values.
 */
public record ConcreteFact(String relation, List<Object> getFields)
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
        var termString = getFields.stream().map(Object::toString)
            .collect(Collectors.joining(", "));
        return relation + "(" + termString + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ConcreteFact fact = (ConcreteFact) o;
        return relation.equals(fact.relation) && getFields.equals(fact.getFields);
    }

    @Override
    public int hashCode() {
        int result = relation.hashCode();
        result = 31 * result + getFields.hashCode();
        return result;
    }
}
