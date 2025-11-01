package com.wjduquette.joe.nero;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Nero {@link Fact} type providing both ordered and named-field
 * access with provided field names.
 *
 * <p>This type is designed for use with Joe's scripted records, which
 * have a list of field names and a HashMap of field values.</p>
 */
public final class PairFact implements Fact {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final String relation;
    private final List<String> fieldNames;
    private final List<Object> fields;
    private Map<String, Object> fieldMap;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new PairFact
     * @param relation The relation
     * @param fieldNames The field names, in the proper order
     * @param fields The field values
     */
    @SuppressWarnings("unused")
    public PairFact(
        String relation,
        List<String> fieldNames,
        List<Object> fields
    ) {
        this.relation = relation;
        this.fieldNames = fieldNames;
        this.fieldMap = null;
        this.fields = fields;
    }

    /**
     * Creates a new PairFact
     * @param relation The relation
     * @param fieldNames The field names, in the proper order
     * @param fieldMap The field map
     */
    public PairFact(
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

    @Override public String        relation()  { return relation; }
    @Override public boolean       isOrdered() { return true; }
    @Override public int           arity()     { return fields.size(); }
    @Override public List<Object>  getFields() { return fields; }

    @Override public Map<String, Object> getFieldMap() {
        // Construct the field map on demand.
        if (fieldMap == null) {
            fieldMap = new HashMap<>();
            for (var i = 0; i < fieldNames.size(); i++) {
                fieldMap.put(fieldNames.get(i), fields.get(i));
            }
        }
        return fieldMap;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        PairFact that = (PairFact) o;
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
        return "PairFact[relation=" + relation + ", fieldNames=" + fieldNames
            + ", fields=" + fields + "]";
    }
}
