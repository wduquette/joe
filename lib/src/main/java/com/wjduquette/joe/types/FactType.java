package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.ProxyType;
import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.ListFact;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A ProxyType for the Fact interface.
 */
public class FactType extends ProxyType<Fact> {
    /** The type, ready for installation. */
    public static final FactType TYPE = new FactType();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the proxy.
     */
    FactType() {
        super("Fact");

        //**
        // @package joe
        // @type Fact
        // An ad hoc type for Nero facts, consisting of a `relation` name
        // and ordered or named fields.  A Nero `ruleset` produces
        // `Fact` values by default, and also accepts `Facts` as input.
        // Many Joe values can be converted to `Facts`.
        proxies(Fact.class);

        staticMethod("of",  this::_of);

        initializer(this::_init);

        method("fieldMap",  this::_fieldMap);
        method("fields",    this::_fields);
        method("isOrdered", this::_isOrdered);
        method("relation",  this::_relation);
        method("toString",  this::_toString);
    }

    //-------------------------------------------------------------------------
    // Stringify

    @Override
    public String stringify(Joe joe, Object value) {
        assert value instanceof Fact;
        var fact = (Fact)value;
        String fields = fact.hasOrderedFields()
            ? fact.getFields().stream()
                .map(joe::stringify)
                .collect(Collectors.joining(", "))
            : fact.getFieldMap().entrySet().stream()
                .map(e -> e.getKey() + ": " + joe.stringify(e.getValue()))
                .collect(Collectors.joining(", "));

        return "Fact(" + fact.relation() + ", " + fields + ")";
    }

    //-------------------------------------------------------------------------
    // Support for instance fields

    /**
     * If the instance has any fields, they are assumed to be ordered.
     * Subclasses can override.
     * @return true or false
     */
    @Override
    public boolean hasOrderedFields(Object value) {
        assert value instanceof Fact;
        return ((Fact)value).hasOrderedFields();
    }

    /**
     * Returns true if the value has a field with the given name, and
     * false otherwise.
     *
     * @param value A value of the proxied type
     * @param fieldName The field name
     * @return true or false
     */
    @SuppressWarnings("unused")
    public boolean hasField(Object value, String fieldName) {
        assert value instanceof Fact;
        return ((Fact)value).getFieldMap().containsKey(fieldName);
    }

    /**
     * Returns a list of the names of the value's fields.  The
     * list will be empty if the value has no fields.
     * @param value A value of the proxied type
     * @return The list
     */
    @SuppressWarnings("unused")
    public List<String> getFieldNames(Object value) {
        assert value instanceof Fact;
        return new ArrayList<>(((Fact)value).getFieldMap().keySet());
    }

    /**
     * Gets the value of the named property.  Throws an
     * "Undefined property" error if there is no such property.
     * @param value A value of the proxied type
     * @param propertyName The property name
     * @return The property value
     */
    @SuppressWarnings({"unused"})
    public Object get(Object value, String propertyName) {
        var method = bind(value, propertyName);

        if (method != null) {
            return method;
        }

        assert value instanceof Fact;
        var map = ((Fact)value).getFieldMap();

        if (map.containsKey(propertyName)) {
            return map.get(propertyName);
        }

        throw new JoeError("Undefined property '" +
            propertyName + "'.");
    }

    /**
     * Sets the value of the named field.
     * @param value A value of the proxied type
     * @param fieldName The field name
     * @param other The value to
     * @return The property value
     */
    @SuppressWarnings("unused")
    public Object set(Object value, String fieldName, Object other) {
        throw new JoeError("Values of type " + name() +
            " have no mutable properties.");
    }


    //-------------------------------------------------------------------------
    // Static Methods

    //**
    // @static of
    // @args relation, fields
    // Creates a new `Fact` given the relation and a list of
    // field values. The `Fact` will be an instance of the Java
    // `ListFact` class.
    private Object _of(Joe joe, Args args) {
        args.exactArity(2, "Fact.of(relation, fields)");
        var relation = joe.toString(args.next());
        var fields = joe.toList(args.next());
        return new ListFact(relation, fields);
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // @args relation, field, ...
    // Creates a new `Fact` given the relation and one or more the
    // field values. The `Fact` will be an instance of the Java
    // `ListFact` class.
    private Object _init(Joe joe, Args args) {
        args.minArity(2, "Fact(relation, field, ...)");
        var relation = joe.toString(args.next());
        var fields = args.remainderAsList();
        return new ListFact(relation, fields);
    }

    //-------------------------------------------------------------------------
    // Instance Method Implementations

    //**
    // @method fieldMap
    // @result Map
    // Returns a read-only map of the field values.
    private Object _fieldMap(Fact value, Joe joe, Args args) {
        args.exactArity(0, "fieldMap()");
        return joe.readonlyMap(value.getFieldMap());
    }

    //**
    // @method fields
    // @result List
    // Returns a read-only list of the field values, if the fact
    // [[List.method.isOrdered]].
    private Object _fields(Fact value, Joe joe, Args args) {
        args.exactArity(0, "fields()");
        if (value.hasOrderedFields()) {
            return joe.readonlyList(value.getFields());
        } else {
            throw joe.expected("fact with ordered fields", value);
        }
    }

    //**
    // @method isOrdered
    // @result Boolean
    // Returns true if the fact has ordered fields, and false otherwise.
    private Object _isOrdered(Fact value, Joe joe, Args args) {
        args.exactArity(0, "isOrdered()");
        return value.hasOrderedFields();
    }

    //**
    // @method relation
    // @result String
    // Returns the Fact's relation name.
    private Object _relation(Fact value, Joe joe, Args args) {
        args.exactArity(0, "relation()");
        return value.relation();
    }

    //**
    // @method toString
    // @result String
    // Returns the value's string representation.
    private Object _toString(Fact value, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return stringify(joe, value);
    }
}
