package com.wjduquette.joe;

import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.ListFact;
import com.wjduquette.joe.nero.Nero;
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
        // FIRST, Build the list of input facts, wrapping values of proxied
        // types as TypedValues so that they can be used as Facts
        // by Nero.
        var heads = rsv.ruleset().getHeadRelations();
        var inputFacts = new HashSet<Fact>();

        for (var input : inputs) {
            // Throws JoeError if the input cannot be converted to a Fact
            var fact = joe.toFact(input);

            if (heads.contains(fact.relation())) {
                throw new JoeError(
                    "Input fact type collides with rule set's head relation: '" +
                    fact.relation() + "'.");
            }
            inputFacts.add(fact);
        }

        // NEXT, Execute the rule set.
        var nero = new Nero(rsv.ruleset());
        nero.setFactFactory(ListFact::new);
        nero.infer(inputFacts);

        // NEXT, build the list of known facts.  We want the input
        // facts as they were given to us, plus the newly inferred
        // facts, converted according to any export declarations.
        var result = new SetValue();
        result.addAll(inputs);

        for (var fact : nero.getInferredFacts()) {
            var creator = rsv.exports().get(fact.relation());

            if (creator != null) {
                result.add(joe.call(creator, fact.getFields().toArray()));
            } else {
                result.add(fact);
            }
        }
        return result;
    }
}
