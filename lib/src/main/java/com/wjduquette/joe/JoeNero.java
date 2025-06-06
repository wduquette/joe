package com.wjduquette.joe;

import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.Nero;
import com.wjduquette.joe.types.FactValue;
import com.wjduquette.joe.types.RuleSetValue;
import com.wjduquette.joe.types.SetValue;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * The JoeNero class wraps the
 * {@link com.wjduquette.joe.nero.Nero} Datalog engine, doing the work to
 * translate between Joe data and Nero data.
 */
public class JoeNero {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    private final RuleSetValue rsv;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new instance of the Nero engine.
     * @param rsv The rule set value
     */
    public JoeNero(Joe joe, RuleSetValue rsv) {
        this.joe = joe;
        this.rsv = rsv;
    }

    //-------------------------------------------------------------------------
    // API

    /**
     * Preliminary implementation of infer().
     * @return The set of known facts.
     * @throws JoeError if the rule set is not stratified.
     */
    public SetValue infer() {
        return infer(List.of());
    }

    /**
     * Preliminary implementation of infer().
     * @param inputs The set of scripted input facts.
     * @return The set of known facts.
     * @throws JoeError if the rule set is not stratified.
     */
    public SetValue infer(Collection<?> inputs) {
        var inputFacts = new HashSet<Fact>();

        for (var input : inputs) {
            if (input instanceof JoeValue fact) {
                if (fact.hasFields()) {
                    inputFacts.add(fact);
                    continue;
                }
            }
            throw joe.expected("fact", input);
        }

        var nero = new Nero(rsv.ruleset());
        nero.setFactFactory(FactValue::new);
        nero.infer(inputFacts);
        return new SetValue(nero.getAllFacts());
    }
}
