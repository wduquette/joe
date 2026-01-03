package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.ProxyType;
import com.wjduquette.joe.nero.Fact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        // An ad hoc type for [[Nero]] and [[NeroDatabase]] facts, consisting
        // of a `relation` name and ordered or named fields.
        //
        // ## Fact Fields and Field Names
        //
        // A `Fact`'s field values are accessible by the
        // [[method:Fact.fields]] method if the `Fact`
        // [[method:Fact.isOrdered]], and as a map of field names and
        // values via the
        // [[method:Fact.fieldMap]] method.
        //
        // In addition, a fact's fields can be accessed as normal Joe object
        // fields:
        //
        // ```joe
        // // Ordered fact; field names and values are ordered.
        // var fact = Fact("Thing", ["id", #car, "color", #red]);
        //
        // // Prints "This Thing is #red"
        // println("This " + fact.relation() + " is " + fact.color);
        //
        // // Unordered fact; field names are as given.
        // var fact2 = Fact("Thing", {"id": #car, "color": #red});
        //
        // // Prints "This Thing is #red"
        // println("This " + fact.relation() + " is " + fact.color);
        // ```
        proxies(Fact.class);

        initializer(this::_init);

        method("fieldMap",    this::_fieldMap);
        method("fieldNames",  this::_fieldNames);
        method("fields",      this::_fields);
        method("isOrdered",   this::_isOrdered);
        method("relation",    this::_relation);
        method("toString",    this::_toString);
    }

    //-------------------------------------------------------------------------
    // Stringify

    @Override
    public String stringify(Joe joe, Object value) {
        assert value instanceof Fact;
        var fact = (Fact)value;
        String fields = fact.isOrdered()
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
     * Returns a list of the names of the value's fields.  The
     * list will be empty if the value has no fields.
     * @param value A value of the proxied type
     * @return The list
     */
    @Override
    public List<String> getFieldNames(Object value) {
        assert value instanceof Fact;
        var fact = (Fact)value;
        return fact.isOrdered()
            ? fact.shape().names()
            : new ArrayList<>(fact.getFieldMap().keySet());
    }

    /**
     * Gets the value of the named property.  Throws an
     * "Undefined property" error if there is no such property.
     * @param value A value of the proxied type
     * @param propertyName The property name
     * @return The property value
     */
    @Override
    public Object get(Joe joe, Object value, String propertyName) {
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
    @Override
    public Object set(Object value, String fieldName, Object other) {
        throw new JoeError("Values of type " + name() +
            " have no mutable properties.");
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // %args relation, pairs
    // %args relation, map
    // Creates a new `Fact` with the given *relation* name. The fields and
    // their values may be provided as a flat list of name/value pairs
    // or as a map of names and values.
    //
    // - If a list of pairs is provided, the fact will be ordered.
    // - If a map is provided, the fact will be unordered.
    // - All names must be valid Joe identifiers.
    private Object _init(Joe joe, Args args) {
        args.exactArity(2, "Fact(relation, pairs) or Fact(relation, map)");
        var relation = joe.toIdentifier(args.next());
        var data = args.next();
        if (data instanceof List<?> pairs) {
            if (pairs.size() % 2 != 0) {
                throw joe.expected("flat list of pairs", data);
            }

            var names = new ArrayList<String>();
            var fields = new ArrayList<>();
            for (var i = 0; i < pairs.size(); i += 2) {
                var name = joe.toIdentifier(pairs.get(i).toString());
                names.add(name);
                fields.add(pairs.get(i+1));
            }
            return new Fact(relation, names, fields);
        } else if (data instanceof Map<?,?> map) {
            var fieldMap = new HashMap<String, Object>();
            for (var e : map.entrySet()) {
                var name = joe.toIdentifier(e.getKey().toString());
                fieldMap.put(name, e.getValue());
            }
            return new Fact(relation, fieldMap);
        } else {
            throw joe.expected("list or map", data);
        }
    }

    //-------------------------------------------------------------------------
    // Instance Method Implementations

    //**
    // @method fieldMap
    // %result Map
    // Returns a read-only map of the field values.
    private Object _fieldMap(Fact value, Joe joe, Args args) {
        args.exactArity(0, "fieldMap()");
        return joe.readonlyMap(value.getFieldMap());
    }

    //**
    // @method fieldNames
    // %result List
    // Returns a read-only list of the fact's field names.  If the
    // fact is ordered the names will be in the same order as the fields.
    private Object _fieldNames(Fact value, Joe joe, Args args) {
        args.exactArity(0, "fieldNames()");
        return joe.readonlyList(getFieldNames(value));
    }

    //**
    // @method fields
    // %result List
    // Returns a read-only list of the field values, if the fact
    // [[method:Fact.isOrdered]].
    private Object _fields(Fact value, Joe joe, Args args) {
        args.exactArity(0, "fields()");
        if (value.isOrdered()) {
            return joe.readonlyList(value.getFields());
        } else {
            throw joe.expected("fact with ordered fields", value);
        }
    }

    //**
    // @method isOrdered
    // %result Boolean
    // Returns true if the fact has ordered fields, and false otherwise.
    private Object _isOrdered(Fact value, Joe joe, Args args) {
        args.exactArity(0, "isOrdered()");
        return value.isOrdered();
    }

    //**
    // @method relation
    // %result String
    // Returns the Fact's relation name.
    private Object _relation(Fact value, Joe joe, Args args) {
        args.exactArity(0, "relation()");
        return value.relation();
    }

    //**
    // @method toString
    // %result String
    // Returns the value's string representation.
    private Object _toString(Fact value, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return stringify(joe, value);
    }
}
