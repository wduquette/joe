package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.ProxyType;
import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.ConcreteFact;

import java.util.ArrayList;
import java.util.HashMap;
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
        //
        // ## Fact Fields and Field Names
        //
        // A `Fact`'s field values are accessible by the
        // [[method:Fact.fields]] method if the `Fact`
        // [[method:Fact.isOrdered]], and as a map of field names and
        // values via the
        // [[method:Fact.fieldMap]] method.
        //
        // In addition, a fact's fields can be access as normal Joe object
        // fields:
        //
        // ```joe
        // // Simple ordered fact; field names are `f1` and `f2`.
        // var fact = Fact("Thing", #car, #red);
        //
        // // Prints "This Thing is #red"
        // println("This " + fact.relation() + " is " + fact.f2);
        //
        // // Simple unordered fact; field names are as given.
        // var fact2 = Fact.ofMap("Thing", {"id": #car, "color": #red});
        //
        // // Prints "This Thing is #red"
        // println("This " + fact.relation() + " is " + fact.color);
        // ```
        proxies(Fact.class);

        staticMethod("ofMap",   this::_ofMap);
        staticMethod("ofPairs", this::_ofPairs);

        initializer(this::_init);

        method("fieldMap",    this::_fieldMap);
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
        return new ArrayList<>(((Fact)value).getFieldMap().keySet());
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
    // Static Methods

    //**
    // @static ofMap
    // %args relation, fieldMap
    // Creates a new unordered `Fact` given the relation and the field map.
    // Its fields will have the names given as keys in the map.
    //
    // The `Fact` will be an instance of the Java `MapFact` class.
    private Object _ofMap(Joe joe, Args args) {
        args.exactArity(2, "Fact.ofMap(relation, fieldMap)");
        var relation = joe.toIdentifier(args.next());
        var map = joe.toMap(args.next());
        var fieldMap = new HashMap<String, Object>();
        for (var e : map.entrySet()) {
            var name = joe.toIdentifier(e.getKey().toString());
            fieldMap.put(name, e.getValue());
        }
        return new ConcreteFact(relation, fieldMap);
    }

    //**
    // @static ofPairs
    // %args relation, pairs
    // Creates a new ordered `Fact` given a flat list of field name/value
    // pairs. Its fields will have the names given in the list.
    //
    // The `Fact` will be an instance of the Java `PairFact` class.
    private Object _ofPairs(Joe joe, Args args) {
        args.exactArity(2, "Fact.ofPairs(relation, pairs)");
        var relation = joe.toIdentifier(args.next());
        var pairsArg = args.next();
        var pairs = joe.toList(pairsArg);

        if (pairs.size() % 2 != 0) {
            throw joe.expected("flat list of pairs", pairsArg);
        }

        var names = new ArrayList<String>();
        var fields = new ArrayList<>();
        for (var i = 0; i < pairs.size(); i += 2) {
            var name = joe.toIdentifier(pairs.get(i).toString());
            names.add(name);
            fields.add(pairs.get(i+1));
        }
        return new ConcreteFact(relation, names, fields);
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // %args relation, field, ...
    // Creates a new `Fact` given the relation and one or more the
    // field values. Its fields will be named `f0`, `f1`, etc.
    //
    // The `Fact` will be an instance of the Java `ListFact` class.
    private Object _init(Joe joe, Args args) {
        args.minArity(2, "Fact(relation, field, ...)");
        // TODO
        throw new JoeError("Fact(...) is currently unsupported.");
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
