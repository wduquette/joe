package com.wjduquette.joe.nero;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A Nero {@link Fact} type providing both ordered and named-field
 * access with provided field names.
 *
 * <p>This type is designed for use with Joe's scripted records, which
 * have a list of field names and a HashMap of field values.</p>
 */
public final class RecordFact implements Fact {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final String relation;
    private final List<String> fieldNames;
    private final Map<String, Object> fieldMap;
    private final List<Object> fields;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * @param relation The relation
     * @param fieldNames The field names, in the proper order
     * @param fieldMap The field map
     */
    public RecordFact(
        String relation,
        List<String> fieldNames,
        Map<String, Object> fieldMap
    ) {
        this.relation = relation;
        this.fieldNames = fieldNames;
        this.fieldMap = fieldMap;
        this.fields = new ArrayList<>(fieldNames.size());
        for (var name : fieldNames) {
            fields.add(fieldMap.get(name));
        }
    }

    /**
     * Gets the fact's field names.
     * @return The names
     */
    public List<String> getFieldNames() {
        return fieldNames;
    }

    //-------------------------------------------------------------------------
    // Fact API

    @Override public String              relation()    { return relation; }
    @Override public boolean             isOrdered()   { return true; }
    @Override public List<Object>        getFields()   { return fields; }
    @Override public Map<String, Object> getFieldMap() { return fieldMap; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        RecordFact that = (RecordFact) o;
        return relation.equals(that.relation)
            && fieldNames.equals(that.fieldNames)
            && fields.equals(that.fields);
    }

    @Override
    public int hashCode() {
        int result = relation.hashCode();
        result = 31 * result + fieldNames.hashCode();
        result = 31 * result + fields.hashCode();
        return result;
    }

    @Override
    public String toString() {
        var mapString = fieldNames.stream()
            .map(n -> n + "=" + fieldMap.get(n))
            .collect(Collectors.joining(", "));
        return "RecordFact[" + relation + ", " + mapString + "]";
    }
}
