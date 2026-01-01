package com.wjduquette.joe.nero;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NewFact {
    //------------------------------------------------------------------------
    // Instance Variables

    private final Shape shape;
    private final List<Object> fields;
    private Map<String,Object> fieldMap;


    //------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new ordered fact given the inputs.
     * @param relation The relation
     * @param names The field names
     * @param fields The field values
     */
    public NewFact(String relation, List<String> names, List<Object> fields) {
        this.shape = new Shape(relation, names);
        this.fieldMap = null;
        this.fields = List.copyOf(fields);

        if (names.size() != fields.size()) {
            throw new IllegalArgumentException("names.size != fields.size");
        }
    }

    /**
     * Creates a new unordered fact given inputs.
     * @param relation The relation
     * @param fieldMap The map of field names and values.
     */
    public NewFact(String relation, Map<String,Object> fieldMap) {
        this.shape = new Shape(relation);
        this.fieldMap = Map.copyOf(fieldMap);
        this.fields = null;
    }

    //------------------------------------------------------------------------
    // API

    /**
     * The fact's shape.
     * @return The shape
     */
    public Shape shape() {
        return shape;
    }

    /**
     * The fact's relation name.
     * @return The relation
     */
    public String relation() {
        return shape.relation();
    }

    /**
     * Gets whether the fact's fields can be accessed positionally via the
     * getFields() method or whether they must be accessed by name.
     * @return true or false
     */
    public boolean isOrdered() {
        return shape.isOrdered();
    }

    /**
     * Returns the number of ordered fields in the fact, or zero if the
     * fact is unordered.
     * @return The number.
     */
    public int arity() {
        return shape.arity();
    }

    /**
     * If isOrdered(), the list of field values.
     * @return the list
     * @throws IllegalStateException if !isOrdered
     */
    public List<Object> getFields() {
        if (fields != null) return fields;
        throw new IllegalStateException(
            "Fact does not have ordered fields!");
    }

    /**
     * Gets the fact's field map.
     * @return The field map
     */
    public Map<String,Object> getFieldMap() {
        if (fieldMap == null) {

            fieldMap = new HashMap<>();
            for (var i = 0; i < fields.size(); i++) {
                fieldMap.put(shape.names().get(i), fields.get(i));
            }
        }
        return Collections.unmodifiableMap(fieldMap);
    }

    /**
     * Gets the value of the named field, or null if it's undefined.
     * @param name The field name
     * @return The value, or null
     */
    public Object get(String name) {
        return getFieldMap().get(name);
    }
}
