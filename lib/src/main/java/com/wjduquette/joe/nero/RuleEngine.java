package com.wjduquette.joe.nero;

import com.wjduquette.joe.JoeError;

import java.util.*;

/**
 * The Nero inference engine.  Given a {@link RuleSet} and a set of
 * input {@link Fact Facts}, the engine will infer the facts implied by
 * the two.
 */
public class RuleEngine {
    public static final String INFER_ERROR =
        "Call `infer()` before querying results.";

    //-------------------------------------------------------------------------
    // Static

    /**
     * Nero's default fact factory; it creates {@link ListFact} objects.
     */
    public static final FactFactory DEFAULT_FACT_FACTORY =
        RuleEngine::defaultFactFactory;

    private static Fact defaultFactFactory(String relation, List<Object> terms) {
        return new ListFact(relation, terms);
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    //
    // Constructor Data
    //

    // The Nero rule set, i.e., the compiled Nero program.
    private final RuleSet ruleset;

    // Map from head relation to rules with that head.
    private final Map<String,List<Rule>> ruleMap = new HashMap<>();

    //
    // Configuration Data
    //

    // The factory used to create new facts.
    private FactFactory factFactory = DEFAULT_FACT_FACTORY;

    // Debug Flag
    private boolean debug = false;

    //
    // Working Data
    //

    // The set of known facts, initialized by the constructor
    private final FactSet knownFacts = new FactSet();

    // Whether the infer() method has been called or not.  We only do
    // inference once per instance of the engine.
    private boolean inferenceComplete = false;

    // Facts inferred from the rule set's axioms and rules (aka,
    // the "intensional database" in Datalog jargon).
    private final Set<Fact> inferredFacts = new HashSet<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new RuleEngine with the given ruleset and no external database
     * of facts.
     * @param ruleset The ruleset
     */
    public RuleEngine(RuleSet ruleset) {
        this.ruleset = ruleset;

        // NEXT, Categorize the rules by head relation
        for (var rule : ruleset.rules()) {
            var head = rule.head().relation();
            var list = ruleMap.computeIfAbsent(head, k -> new ArrayList<>());
            list.add(rule);
        }
    }

    /**
     * Creates a new RuleEngine with the given ruleset and the given set of
     * input facts.
     * @param ruleset The ruleset
     * @param facts The fact set
     */
    public RuleEngine(RuleSet ruleset, FactSet facts) {
        this(ruleset);
        knownFacts.addAll(facts);
    }

    //-------------------------------------------------------------------------
    // Configuration

    /**
     * Gets whether debugging is enabled or not.
     * @return true or false
     */
    @SuppressWarnings("unused")
    public boolean isDebug() {
        return debug;
    }

    /**
     * Enables/disables debugging.  When debugging is enabled, Nero will
     * dump a great deal of debugging information that can be used to
     * debug the rule set, and also Nero itself.
     * @param debug true or false
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Gets the factory used to create new {@link Fact Facts} given a
     * relation and list of terms.
     * @return The factory
     */
    @SuppressWarnings("unused")
    public FactFactory getFactFactory() {
        return factFactory;
    }

    /**
     * Sets the factory used to create new {@link Fact Facts} given a
     * relation and list of terms.
     * @param factFactory The factory
     */
    @SuppressWarnings("unused")
    public void setFactFactory(FactFactory factFactory) {
        this.factFactory = factFactory;
    }

    //-------------------------------------------------------------------------
    // Queries

    /**
     * Gets the facts inferred from rule set axioms.
     * @return The axiomatic facts.
     */
    public Set<Fact> getAxioms() {
        return ruleset.facts();
    }

    /**
     * Gets all facts known after inference is complete.
     * @return The known facts.
     */
    public FactSet getKnownFacts() {
        if (!inferenceComplete) {
            throw new IllegalStateException(INFER_ERROR);
        }
        return knownFacts;
    }

    /**
     * Gets the facts inferred from the inputs given the axioms and rules.
     * @return The inferred facts.
     */
    public Set<Fact> getInferredFacts() {
        if (!inferenceComplete) {
            throw new IllegalStateException(INFER_ERROR);
        }
        return inferredFacts;
    }

    //-------------------------------------------------------------------------
    // Inference

    /**
     * Executes the inference algorithm, computing all facts knowable
     * from the axioms and rules given the input facts
     * (the "extensional database").
     * @throws JoeError if the rule set is not stratified.
     */
    public void infer() {
        // FIRST, check stratification
        if (!ruleset.isStratified()) {
            throw new JoeError("Rule set is not stratified.");
        }

        // NEXT, check for rule/inputs relation collision
        for (var relation : knownFacts.getRelations()) {
            if (ruleMap.containsKey(relation)) {
                throw new JoeError(
                    "Rule head relation collides with input fact relation: '" +
                        relation + "'.");
            }
        }

        // NEXT, only do inference once.
        if (inferenceComplete) return;
        inferenceComplete = true;

        // NEXT, initialize the data structures
        knownFacts.addAll(ruleset.facts());
        inferredFacts.addAll(ruleset.facts());

        // NEXT, execute the rules.
        if (debug) {
            System.out.println("Rule Strata: " +
                ruleset.getStrata());
        }

        for (var i = 0; i < ruleset.getStrata().size(); i++) {
            inferStratum(i, ruleset.getStrata().get(i));
        }
    }

    private void inferStratum(int stratum, List<String> heads) {
        int count = 0;
        boolean gotNewFact;
        do {
            gotNewFact = false;
            if (debug) System.out.println("Iteration " + stratum + "." + (++count) + ":");
            for (var head : heads) {
                for (var rule : ruleMap.get(head)) {
                    if (matchRule(rule)) gotNewFact = true;
                }
            }
        } while (gotNewFact);
    }

    // Matches the rule against the known facts, adding any new facts and
    // returning true if there were any and false otherwise.
    private boolean matchRule(Rule rule) {
        if (debug) System.out.println("  Rule: " + rule);

        var bc = new BindingContext(rule);

        matchBodyAtom(bc, 0);

        if (!bc.inferredFacts.isEmpty()) {
            knownFacts.addAll(bc.inferredFacts);
            inferredFacts.addAll(bc.inferredFacts);
            return true;
        } else {
            return false;
        }
    }

    // Matches the rule's index-th body atom against the relevant facts.
    private void matchBodyAtom(BindingContext bc, int index) {
        var atom = bc.rule.body().get(index);
        var facts = knownFacts.getRelation(atom.relation());

        // FIRST, Save the current bindings, as we will begin with them for each
        // fact.
        var givenBindings = bc.bindings;

        for (var fact : facts) {
            // FIRST, Copy the bindings for this fact.
            bc.bindings = new Bindings(givenBindings);

            // NEXT, Verify that the fact can be matched by this atom.
            if (atom.requiresOrderedFields() && !fact.isOrdered()) {
                throw new JoeError(
                    "'" + atom.relation() +
                        "' in rule '" + bc.rule +
                        "' requires ordered fields, but a provided " +
                        "fact is not ordered.");
            }

            // NEXT, if it doesn't match, go on to the next fact.
            // TODO: Improve the `BodyAtom::matches` API
            bc.bindings = atom.matches(fact, bc.bindings);
            if (bc.bindings == null) continue;

            // NEXT, it matches.  If there's another body atom, check it and
            // then go on to the next fact.
            if (index + 1 < bc.rule.body().size()) {
                matchBodyAtom(bc, index + 1);
                continue;
            }

            // NEXT, we've matched all body atoms.  Check the bindings against
            // the constraints.  If they are not met, continue with the
            // next fact.
            if (!constraintsMet(bc)) continue;

            // NEXT, check each negation.
            if (!checkNegations(bc)) continue;

            // NEXT, the rule has matched.  Build the inferred fact, and see
            // if it's actually new.
            var newFact = createFact(bc);
            if (!knownFacts.getAll().contains(newFact)) {
                bc.inferredFacts.add(newFact);
                if (debug) System.out.println("      Fact: " + newFact);
            }
        }
    }

    private boolean constraintsMet(BindingContext bc) {
        for (var constraint : bc.rule.constraints()) {
            if (!constraintMet(constraint, bc.bindings)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkNegations(BindingContext bc) {
        for (var atom : bc. rule.negations()) {
            for (var fact : knownFacts.getRelation(atom.relation())) {
                if (atom.matches(fact, bc.bindings) != null) {
                    return false;
                }
            }
        }
        return true;
    }

    private Fact createFact(BindingContext bc) {
        var terms = new ArrayList<>();

        for (var term : bc.rule.head().terms()) {
            switch (term) {
                case Constant c -> terms.add(c.value());
                case Variable v -> terms.add(bc.bindings.get(v));
                case Wildcard ignored -> throw new IllegalStateException(
                    "Rule head contains a Wildcard term.");
            }
        }

        return factFactory.create(bc.rule.head().relation(), terms);
    }

    private boolean constraintMet(
        Constraint constraint,
        Map<Variable,Object> bindings
    ) {
        var a = bindings.get(constraint.a());

        var b = switch (constraint.b()) {
            case Variable v -> bindings.get(v);
            case Constant c -> c.value();
            case Wildcard ignored -> throw new IllegalStateException(
                "Constraint contains a Wildcard term.");
        };

        return switch (constraint.op()) {
            case EQ -> Objects.equals(a, b);
            case NE -> !Objects.equals(a, b);
            case GT -> {
                if (a instanceof Double d1 && b instanceof Double d2) {
                    yield d1 > d2;
                } else if (a instanceof String s1 && b instanceof String s2) {
                    yield s1.compareTo(s2) > 0;
                } else {
                    yield false;
                }
            }
            case GE -> {
                if (a instanceof Double d1 && b instanceof Double d2) {
                    yield d1 >= d2;
                } else if (a instanceof String s1 && b instanceof String s2) {
                    yield s1.compareTo(s2) >= 0;
                } else {
                    yield false;
                }
            }
            case LT -> {
                if (a instanceof Double d1 && b instanceof Double d2) {
                    yield d1 < d2;
                } else if (a instanceof String s1 && b instanceof String s2) {
                    yield s1.compareTo(s2) < 0;
                } else {
                    yield false;
                }
            }
            case LE -> {
                if (a instanceof Double d1 && b instanceof Double d2) {
                    yield d1 <= d2;
                } else if (a instanceof String s1 && b instanceof String s2) {
                    yield s1.compareTo(s2) <= 0;
                } else {
                    yield false;
                }
            }
        };
    }

    //-------------------------------------------------------------------------
    // Helpers

    // The context for the recursive matchBodyAtom method.
    private static class BindingContext {
        private final Rule rule;
        private final List<Fact> inferredFacts = new ArrayList<>();
        private Bindings bindings = new Bindings();

        BindingContext(Rule rule) {
            this.rule = rule;
        }
    }

}
