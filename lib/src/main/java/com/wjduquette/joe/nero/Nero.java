package com.wjduquette.joe.nero;

import com.wjduquette.joe.JoeError;

import java.util.*;

/**
 * The Nero inference engine.  Given a {@link RuleSet}, and optionally
 * an additional set of input {@link Fact Facts}, Nero will infer the
 * new Facts implied by the input and the RuleSet's {@link Rule Rules}.
 */
public class Nero {
    //-------------------------------------------------------------------------
    // Static

    /**
     * Nero's default fact factory; it creates {@link ConcreteFact} objects.
     */
    public static final FactFactory DEFAULT_FACT_FACTORY =
        Nero::defaultFactFactory;

    private static Fact defaultFactFactory(String relation, List<Object> terms) {
        return new ConcreteFact(relation, terms);
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    // Map from head relation to rules with that head.
    private final Map<String,List<Rule>> ruleMap = new HashMap<>();

    // Head relations by stratum.
    private final boolean isStratified;
    private final List<List<String>> strata;

    // Facts as read from the Nero program.
    private final Set<Fact> baseFacts = new HashSet<>();

    // The current set of known facts.
    private final Set<Fact> knownFacts = new HashSet<>();

    // Facts by relation
    private final Map<String, List<Fact>> factMap = new HashMap<>();

    // Fact Creator
    private FactFactory factFactory = DEFAULT_FACT_FACTORY;

    // Debug Flag
    private boolean debug = false;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an instance of Nero for the given {@link RuleSet}.
     * @param ruleset The rule set.
     */
    public Nero(RuleSet ruleset) {
        // FIRST, analyze the rule set
        var graph = new Stratifier(ruleset.getRules());
        this.isStratified = graph.isStratified();

        this.strata = isStratified ? graph.strata() : null;

        if (debug) System.out.println("Rule Strata: " + strata);

        // NEXT, Categorize the rules by head relation
        for (var rule : ruleset.getRules()) {
            var head = rule.head().relation();
            var list = ruleMap.computeIfAbsent(head, k -> new ArrayList<>());
            list.add(rule);
        }

        // NEXT, save the base facts.
        this.baseFacts.addAll(ruleset.getFacts());
        baseFacts.forEach(this::addFact);
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

    /**
     * Gets whether the rule set can be stratified or not.
     * @return true or false.
     */
    public boolean isStratified() {
        return isStratified;
    }

    /**
     * Gets the known facts that have the given relation.
     * @param relation The relation
     * @return The facts.
     */
    public List<Fact> getFacts(String relation) {
        return factMap.computeIfAbsent(relation,
            key -> new ArrayList<>());
    }

    /**
     * Gets all known facts.
     * @return The facts.
     */
    public Set<Fact> getAllFacts() {
        return Collections.unmodifiableSet(knownFacts);
    }

    /**
     * Gets any new facts inferred by Nero.
     * @return The new facts.
     */
    public Set<Fact> getNewFacts() {
        var result = new HashSet<Fact>();
        for (var fact : knownFacts) {
            if (!baseFacts.contains(fact)) result.add(fact);
        }

        return result;
    }

    /**
     * Adds the fact into the set of known facts.  Returns true if the
     * fact was previously unknown, and false otherwise.
     * Eventually this will be public, but we'll need to extend the
     * data model.
     * @param fact The fact
     * @return true or false
     */
    private boolean addFact(Fact fact) {
        if (!knownFacts.contains(fact)) {
            var list = getFacts(fact.relation());
            list.add(fact);

            knownFacts.add(fact);
            return true;
        } else {
            return false;
        }
    }


    //-------------------------------------------------------------------------
    // Inference

    /**
     * Executes the inference algorithm, computing all facts knowable
     * from the rules.
     * @throws JoeError if the rule set is not stratified.
     */
    public void infer() {
        if (!isStratified) {
            throw new JoeError("Rule set is not stratified.");
        }

        knownFacts.clear();
        factMap.clear();
        baseFacts.forEach(this::addFact);

        for (var i = 0; i < strata.size(); i++) {
            infer(i, strata.get(i));
        }
    }

    private void infer(int stratum, List<String> heads) {
        int count = 0;
        boolean gotNewFact;
        do {
            gotNewFact = false;
            if (debug) System.out.println("Iteration " + stratum + "." + (++count) + ":");
            for (var head : heads) {
                for (var rule : ruleMap.get(head)) {
                    if (debug) System.out.println("  Rule: " + rule);

                    var iter = new TupleIterator(rule);
                    while (iter.hasNext()) {
                        var tuple = iter.next();
                        var fact = matchRule(rule, tuple);

                        if (fact != null && addFact(fact)) {
                            if (debug) System.out.println("    Fact: " + fact);
                            gotNewFact = true;
                        }
                    }
                }
            }
        } while (gotNewFact);
    }

    private Fact matchRule(Rule rule, Fact[] tuple) {
        // FIRST, match each pattern in the rule with a fact from
        // the tuple, binding variables from left to right.
        var bindings = new Bindings();

        for (int i = 0; i < tuple.length; i++) {
            var b = rule.body().get(i);

            bindings = b.matches(tuple[i], bindings);
            if (bindings == null) return null;
        }

        // NEXT, check the bindings against the constraints.
        for (var c : rule.constraints()) {
            if (!constraintMet(c, bindings)) {
                return null;
            }
        }

        // NEXT, check the bindings against the negations.
        for (var atom : rule.negations()) {
            for (var fact : getFacts(atom.relation())) {
                if (atom.matches(fact, bindings) != null) {
                    return null;
                }
            }
        }

        // NEXT, build the list of terms and return the new fact.
        var terms = new ArrayList<>();

        for (var term : rule.head().terms()) {
            switch (term) {
                case Constant c -> terms.add(c.value());
                case Variable v -> terms.add(bindings.get(v));
                case Wildcard ignored -> throw new IllegalStateException(
                    "Rule head contains a Wildcard term.");
            }
        }

        return factFactory.create(rule.head().relation(), terms);
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
    // TupleIterator

    private class TupleIterator {
        //---------------------------------------------------------------------
        // Instance Variables

        private final List<List<Fact>> inputs = new ArrayList<>();
        private Fact[] tuple = null;
        private final int total;
        private int next = 0;

        //---------------------------------------------------------------------
        // Constructor

        TupleIterator(Rule rule) {
            // FIRST, get the lists of facts in the order referenced in the
            // rule's body.
            int sum = 0;
            for (var b : rule.body()) {
                var relation = b.relation();
                var facts = getFacts(relation);

                if (facts.isEmpty()) {
                    inputs.clear();
                    total = 0;
                    return;
                }
                inputs.add(facts);
                sum += facts.size();
            }

            // NEXT, save the max number of tuples
            total = sum;
            tuple = new Fact[inputs.size()];
        }

        //---------------------------------------------------------------------
        // API

        public boolean hasNext() {
            return total != next;
        }

        public Fact[] next() {
            if (!hasNext()) {
                throw new IllegalStateException("Iterator is empty.");
            }

            var n = next++;
            var indices = new int[tuple.length];

            for (var i = 0; i < inputs.size(); i++) {
                var input = inputs.get(i);
                indices[i] = n % input.size();
                tuple[i] = input.get(indices[i]);
                n /= input.size();
            }

            return tuple;
        }
    }
}
