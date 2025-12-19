package com.wjduquette.joe.types;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.nero.*;

import java.util.*;

/**
 * A RuleSetValue wraps up a Nero
 * {@link NeroRuleSet} with the additional data
 * required to make use of it in Joe scripts.  RuleSetValues are created
 * by Joe's `ruleset` declaration.
 */
public class RuleSetValue {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final NeroRuleSet ruleset;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a RuleSetValue.
     * @param ruleset The Nero rule set.
     */
    public RuleSetValue(NeroRuleSet ruleset) {
        this.ruleset = ruleset;
    }

    //-------------------------------------------------------------------------
    // API

    /**
     * Gets the underlying Nero rule set.
     * @return The rule set
     */
    public NeroRuleSet ruleset() {
        return ruleset;
    }
    /**
     * Gets whether the rule set is stratified or not.
     * @return true or false.
     */
    public boolean isStratified() {
        return ruleset.isStratified();
    }
}
