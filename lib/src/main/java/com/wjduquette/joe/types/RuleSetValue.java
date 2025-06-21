package com.wjduquette.joe.types;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeNero;
import com.wjduquette.joe.nero.RuleSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A RuleSetValue wraps up a Nero
 * {@link com.wjduquette.joe.nero.RuleSet} with the additional data
 * required to make use of it in Joe scripts.  RuleSetValues are created
 * by Joe's `ruleset` declaration.
 */
public class RuleSetValue {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final String name;
    private final RuleSet ruleset;
    private final Map<String, Object> exports;
    private boolean debug = false;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a RuleSetValue.
     * @param name The rule set's name.
     * @param ruleset The Nero rule set.
     * @param exports map from relation to export callable
     */
    public RuleSetValue(
        String name,
        RuleSet ruleset,
        Map<String, Object> exports
    ) {
        this.name = name;
        this.ruleset = ruleset;
        this.exports = exports;
    }

    //-------------------------------------------------------------------------
    // API

    public String name() {
        return name;
    }

    /**
     * Gets the underlying Nero rule set.
     * @return The rule set
     */
    public RuleSet ruleset() {
        return ruleset;
    }

    /**
     * Gets the map from exported relation to factory callable
     * for exported facts.
     * @return The map
     */
    public Map<String,Object> exports() {
        return Collections.unmodifiableMap(exports);
    }

    /**
     * Gets whether the debug flag has been set or not.
     * @return The flag
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Sets the debug flag.  If enabled, the Nero engine will output
     * a detailed trace of its activities.
     * @param debug The flag
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

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
