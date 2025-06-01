package com.wjduquette.joe.types;

import com.wjduquette.joe.JoeNero;
import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.RuleSet;

import java.util.Set;

/**
 * A RuleSetValue wraps up a Nero
 * {@link com.wjduquette.joe.nero.RuleSet} with the additional data
 * required to make use of it in Joe scripts.  It is the type returned
 * by Joe's `ruleset` expression.
 * @param ruleset The Nero rule set.
 */
public record RuleSetValue(RuleSet ruleset) {
    /**
     * Preliminary implementation of infer().
     * @return The set of all known facts.
     */
    public Set<Fact> infer() {
        var nero = new JoeNero(this);
        return nero.infer();
    }

    /**
     * Gets whether the rule set is stratified or not.
     * @return true or false.
     */
    public boolean isStratified() {
        return ruleset.isStratified();
    }
}
