package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.ProxyType;

import java.util.stream.Collectors;

/**
 * A ProxyType for the FactValue type.
 */
public class FactType extends ProxyType<FactValue> {
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
        proxies(FactValue.class);

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
    // Static Methods

    //**
    // @static of
    // @args relation, fields...
    // Creates a new `Fact` given the relation and one or more field values.
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
    private Object _init(Joe joe, Args args) {
        args.exactArity(1, "init(relation, fields)");
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
    private Object _fields(FactValue value, Joe joe, Args args) {
        args.exactArity(0, "fields()");
        return new ListValue(value.fields());
    }

    //**
    // @method relation
    // @result String
    // Returns the Fact's relation name.
    private Object _relation(FactValue value, Joe joe, Args args) {
        args.exactArity(0, "relation()");
        return value.relation();
    }

    //**
    // @method toString
    // @result String
    // Returns the value's string representation.
    private Object _toString(FactValue value, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return stringify(joe, value);
    }
}
