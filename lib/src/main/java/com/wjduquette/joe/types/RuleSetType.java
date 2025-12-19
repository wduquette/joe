package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.ProxyType;
import com.wjduquette.joe.nero.NeroRuleSet;

/**
 * A ProxyType for the NeroRuleSet type.
 */
public class RuleSetType extends ProxyType<NeroRuleSet> {
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
        // rule set contains Nero axioms and rules, and can be executed
        // using the [[Nero]] tye.
        proxies(NeroRuleSet.class);

        method("isStratified",   this::_isStratified);
        method("toString",       this::_toString);
    }

    //-------------------------------------------------------------------------
    // Stringify

    @Override
    public String stringify(Joe joe, Object value) {
        assert value instanceof NeroRuleSet;
        var rules = (NeroRuleSet)value;

        var buff = new StringBuilder();
        buff.append("ruleset {\n");
        for (var fact : rules.axioms()) {
            buff.append("    ").append(fact).append(";\n");
        }
        for (var rule : rules.rules()) {
            buff.append("    ").append(rule).append("\n");
        }
        buff.append("}");

        return buff.toString();
    }

    //-------------------------------------------------------------------------
    // Instance Method Implementations

    //**
    // @method isStratified
    // %result Boolean
    // Returns whether the rule set is stratified or not.
    private Object _isStratified(NeroRuleSet value, Joe joe, Args args) {
        args.exactArity(0, "isStratified()");
        return value.isStratified();
    }

    //**
    // @method toString
    // %result String
    // Returns the value's string representation.
    private Object _toString(NeroRuleSet value, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return stringify(joe, value);
    }
}
