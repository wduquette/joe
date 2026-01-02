package com.wjduquette.joe.nero;

import java.util.*;

public final class Fact {
    //------------------------------------------------------------------------
    // Instance Variables

    private final Shape shape;
    private final List<Object> fields;
    private final Map<String,Object> fieldMap;


    //------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new ordered fact given the inputs.
     * @param relation The relation
     * @param names The field names
     * @param fields The field values
     */
    public Fact(String relation, List<String> names, List<Object> fields) {
        this.shape = new Shape(relation, names);
        this.fields = List.copyOf(fields);

        if (names.size() != fields.size()) {
            throw new IllegalArgumentException("names.size != fields.size");
        }

        var map = new HashMap<String,Object>();
        for (var i = 0; i < fields.size(); i++) {
            map.put(shape.names().get(i), fields.get(i));
        }
        this.fieldMap = Collections.unmodifiableMap(map);
    }

    /**
     * Creates a new fact given the inputs.  The fact will be ordered
     * if names is not empty, and unordered otherwise.  The field map
     * need not contain a value for every name.
     * @param relation The relation
     * @param names The field names
     * @param fieldMap The field map
     */
    public Fact(String relation, List<String> names, Map<String,Object> fieldMap) {
        this.shape = new Shape(relation, names);
        this.fieldMap = Map.copyOf(fieldMap);

        if (!names.isEmpty()) {
            var list = new ArrayList<>();
            for (var name : names) {
                list.add(fieldMap.get(name));
            }
            this.fields = Collections.unmodifiableList(list);
        } else {
            this.fields = null;
        }
    }

    /**
     * Creates a new unordered fact given inputs.
     * @param relation The relation
     * @param fieldMap The map of field names and values.
     */
    public Fact(String relation, Map<String,Object> fieldMap) {
        this.shape = new Shape(relation);
        this.fieldMap = Map.copyOf(fieldMap);
        this.fields = null;
    }

    /**
     * Creates a copy of another fact with a new relation name.
     * @param relation The new relation
     * @param other The other fact.
     */
    public Fact(String relation, Fact other) {
        this.shape = new Shape(relation, other.shape.names());
        this.fieldMap = other.fieldMap;
        this.fields = other.fields;
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
    @SuppressWarnings("unused")
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
        return fieldMap;
    }

    /**
     * Gets the value of the named field, or null if it's undefined.
     * @param name The field name
     * @return The value, or null
     */
    public Object get(String name) {
        return getFieldMap().get(name);
    }

    //------------------------------------------------------------------------
    // Object API


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Fact other = (Fact) o;
        return shape.equals(other.shape) &&
            Objects.equals(fields, other.fields) &&
            fieldMap.equals(other.fieldMap);
    }

    @Override
    public int hashCode() {
        int result = shape.hashCode();
        result = 31 * result + Objects.hashCode(fields);
        result = 31 * result + fieldMap.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Fact[" + shape + ", " + fieldMap + "]";
    }
}
