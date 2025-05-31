package com.wjduquette.joe;

import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.Nero;
import com.wjduquette.joe.types.FactValue;
import com.wjduquette.joe.types.RuleSetValue;

import java.util.Set;

/**
 * The JoeNero class wraps the
 * {@link com.wjduquette.joe.nero.Nero} Datalog engine, doing the work to
 * translate between Joe data and Nero data.
 */
public class JoeNero {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final RuleSetValue rsv;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new instance of the Nero engine.
     * @param rsv The rule set value
     */
    public JoeNero(RuleSetValue rsv) {
        this.rsv = rsv;
    }

    //-------------------------------------------------------------------------
    // API

    /**
     * Preliminary implementation of infer().
     * @return The set of known facts.
     * @throws JoeError if the rule set is not stratified.
     */
    public Set<Fact> infer() {
        var nero = new Nero(rsv.ruleset());
        nero.setFactFactory(FactValue::new);
        nero.infer();
        return nero.getAllFacts();
    }
}
