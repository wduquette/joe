package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.ProxyType;

/**
 * A ProxyType for the RuleSetValue type.
 */
public class RuleSetType extends ProxyType<RuleSetValue> {
    /** The type, ready for installation. */
    public static final RuleSetType TYPE = new RuleSetType();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the proxy.
     */
    RuleSetType() {
        super("RuleSet");

        //**
        // @package joe
        // @type RuleSet
        // A Nero rule set, as created by the `ruleset` expression.  A
        // rule set contains Nero rules and optionally some number of
        // base facts.  A rule set can infer new facts given its rules,
        // base facts, and any input facts provided by the script.
        proxies(RuleSetValue.class);

        method("infer",     this::_infer);
        method("toString",  this::_toString);
    }

    //-------------------------------------------------------------------------
    // Stringify

    @Override
    public String stringify(Joe joe, Object value) {
        assert value instanceof RuleSetValue;
        var rsv = (RuleSetValue)value;

        var buff = new StringBuilder();
        buff.append("ruleset {\n");
        for (var fact : rsv.ruleset().facts()) {
            buff.append("    ").append(fact).append(";\n");
        }
        for (var rule : rsv.ruleset().rules()) {
            buff.append("    ").append(rule).append("\n");
        }
        buff.append("}");

        return buff.toString();
    }

    //-------------------------------------------------------------------------
    // Instance Method Implementations

    //**
    // @method infer
    // @result Set
    // Returns a set of the known facts, both base and inferred.
    private Object _infer(RuleSetValue value, Joe joe, Args args) {
        args.exactArity(0, "infer()");
        return value.infer();
    }

    //**
    // @method toString
    // @result String
    // Returns the value's string representation.
    private Object _toString(RuleSetValue value, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return stringify(joe, value);
    }
}
