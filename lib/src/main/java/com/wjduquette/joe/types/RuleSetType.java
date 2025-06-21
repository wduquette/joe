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

        method("infer",        this::_infer);
        method("isDebug",      this::_isDebug);
        method("isStratified", this::_isStratified);
        method("name",         this::_name);
        method("setDebug",     this::_setDebug);
        method("toString",     this::_toString);
    }

    //-------------------------------------------------------------------------
    // Stringify

    @Override
    public String stringify(Joe joe, Object value) {
        assert value instanceof RuleSetValue;
        var rsv = (RuleSetValue)value;

        var buff = new StringBuilder();
        buff.append("ruleset ").append(rsv.name()).append(" {\n");
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
    // @args [inputs]
    // @result Set
    // Returns a set of known facts, both input and inferred, given
    // the rule set and any provided *inputs*.
    //
    // **NOTE:** By default, a rule set cannot infer facts of the same
    // types as the given *inputs*: `infer(inputs)` throws an error
    // if any input fact's type name is the same as a relation used in
    // one of the rule set's rule heads or axioms.
    //
    // However, an `export` declaration can achieve the same effect.
    // See the Nero tutorial for details.

    private Object _infer(RuleSetValue value, Joe joe, Args args) {
        args.arityRange(0, 1, "infer([inputs])");
        if (args.isEmpty()) {
            return value.infer(joe);
        } else {
            return value.infer(joe, joe.toCollection(args.next()));
        }
    }

    //**
    // @method isDebug
    // @result Boolean
    // Returns the rule set's debug flag.
    private Object _isDebug(RuleSetValue value, Joe joe, Args args) {
        args.exactArity(0, "isDebug()");
        return value.isDebug();
    }

    //**
    // @method isStratified
    // @result Boolean
    // Returns whether the rule set is stratified or not.
    private Object _isStratified(RuleSetValue value, Joe joe, Args args) {
        args.exactArity(0, "isStratified()");
        return value.isStratified();
    }

    //**
    // @method name
    // @result String
    // Returns the rule set's name.
    private Object _name(RuleSetValue value, Joe joe, Args args) {
        args.exactArity(0, "name()");
        return value.name();
    }

    //**
    // @method setDebug
    // @args flag
    // Sets the rule set's debug flag.  If enabled,
    // [[RuleSet#method.infer]] will output a detailed execution trace.

    private Object _setDebug(RuleSetValue value, Joe joe, Args args) {
        args.exactArity(1, "setDebug(flag)");
        value.setDebug(joe.toBoolean(args.next()));
        return null;
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
