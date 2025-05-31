package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.ProxyType;
import com.wjduquette.joe.nero.Fact;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A ProxyType for the FactValue type.
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
        // An ad hoc type for Nero facts, consisting of a relation name
        // and a list of field values.  A Nero `ruleset` will accept
        // Facts as input and produces Facts as output by default.
        //
        // A Fact's fields have names `f0`, `f1`, ....
        proxies(Fact.class);

        staticMethod("of",  this::_of);

        initializer(this::_init);

        method("fields",    this::_fields);
        method("relation",  this::_relation);
        method("toString",  this::_toString);
    }

    //-------------------------------------------------------------------------
    // Stringify

    @Override
    public String stringify(Joe joe, Object value) {
        assert value instanceof FactValue;
        var fact = (FactValue)value;
        var fields = fact.fields().stream()
            .map(joe::stringify)
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
    public boolean hasOrderedFields() {
        return true;
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
        assert value instanceof FactValue;
        return ((FactValue)value).fieldMap().containsKey(fieldName);
    }

    /**
     * Returns a list of the names of the value's fields.  The
     * list will be empty if the value has no fields.
     * @param value A value of the proxied type
     * @return The list
     */
    @SuppressWarnings("unused")
    public List<String> getFieldNames(Object value) {
        assert value instanceof FactValue;
        return new ArrayList<>(((FactValue)value).fieldMap().keySet());
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

        assert value instanceof FactValue;
        var map = ((FactValue)value).fieldMap();

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
    // @args relation, fields...
    // Creates a new `Fact` given the relation and one or more field values.
    // The `Fact` will be an instance of the Java `FactValue` class.
    private Object _of(Joe joe, Args args) {
        args.minArity(2, "Fact.of(relation, field, ...)");
        var relation = joe.toString(args.next());
        var fields = args.remainderAsList();
        return new FactValue(relation, fields);
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // @args relation, fields
    // Creates a new `Fact` given the relation and a list of field values.
    // The `Fact` will be an instance of the Java `FactValue` class.
    private Object _init(Joe joe, Args args) {
        args.exactArity(2, "Fact(relation, fields)");
        var relation = joe.toString(args.next());
        var fields = joe.toList(args.next());
        return new FactValue(relation, fields);
    }

    //-------------------------------------------------------------------------
    // Instance Method Implementations

    //**
    // @method fields
    // @result List
    // Returns a list of the field values.
    private Object _fields(Fact value, Joe joe, Args args) {
        args.exactArity(0, "fields()");
        return new ListValue(value.fields());
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
