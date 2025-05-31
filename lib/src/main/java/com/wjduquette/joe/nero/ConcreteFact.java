package com.wjduquette.joe.nero;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The default concrete Fact type for use with {@link Nero}.
 * @param relation The relation
 * @param fields The values.
 */
public record ConcreteFact(String relation, List<Object> fields)
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
        var termString = fields.stream().map(Object::toString)
            .collect(Collectors.joining(", "));
        return relation + "(" + termString + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ConcreteFact fact = (ConcreteFact) o;
        return relation.equals(fact.relation) && fields.equals(fact.fields);
    }

    @Override
    public int hashCode() {
        int result = relation.hashCode();
        result = 31 * result + fields.hashCode();
        return result;
    }
}
