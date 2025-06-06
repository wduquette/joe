package com.wjduquette.joe.types;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeNero;
import com.wjduquette.joe.nero.RuleSet;

import java.util.Collection;

/**
 * A RuleSetValue wraps up a Nero
 * {@link com.wjduquette.joe.nero.RuleSet} with the additional data
 * required to make use of it in Joe scripts.  It is the type returned
 * by Joe's `ruleset` expression.
 * @param ruleset The Nero rule set.
 */
public record RuleSetValue(RuleSet ruleset) {
    /**
     * Infer facts from the rule set.
     * @return The set of all known facts.
     */
    public SetValue infer(Joe joe) {
        var nero = new JoeNero(joe, this);
        return nero.infer();
    }

    /**
     * Infer facts from the rule set and the set of scripted input facts.
     * @param inputs The input facts
     * @return The set of all known facts.
     */
    public SetValue infer(Joe joe, Collection<?> inputs) {
        var nero = new JoeNero(joe, this);
        return nero.infer(inputs);
    }

    /**
     * Gets whether the rule set is stratified or not.
     * @return true or false.
     */
    public boolean isStratified() {
        return ruleset.isStratified();
    }
}
