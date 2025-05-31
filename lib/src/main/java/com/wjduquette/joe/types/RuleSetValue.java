package com.wjduquette.joe.types;

import com.wjduquette.joe.nero.RuleSet;

/**
 * A RuleSetValue wraps up a Nero
 * {@link com.wjduquette.joe.nero.RuleSet} with the additional data
 * required to make use of it in Joe scripts.  It is the type returned
 * by Joe's `ruleset` expression.
 * @param ruleset The Nero rule set.
 */
public record RuleSetValue(RuleSet ruleset) {

}
