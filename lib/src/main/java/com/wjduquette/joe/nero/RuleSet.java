package com.wjduquette.joe.nero;

import java.util.*;
import static com.wjduquette.joe.nero.Term.*;

/**
 * A Nero rule base, including all facts and rules read from Nero input.
 * For now, we only consider these; later, we'll be able to add in facts
 * from outside, and merge rule sets.
 */
public class RuleSet {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final List<Rule> rules = new ArrayList<>();

    // Facts as read from the Nero program.
    private final List<Atom> baseFacts = new ArrayList<>();

    // The current set of known facts.
    private final Set<Atom> knownFacts = new HashSet<>();

    // Facts by relation
    private final Map<String, List<Atom>> factMap = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    public RuleSet(List<Rule> rules, List<Atom> baseFacts) {
        this.rules.addAll(rules);
        this.baseFacts.addAll(baseFacts);
        baseFacts.forEach(this::addFact);
    }

    //-------------------------------------------------------------------------
    // API

    /**
     * Adds the fact into the set of known facts.  Returns true if the
     * fact was previously unknown, and false otherwise.
     * Eventually this will be public, but we'll need to extend the
     * data model.
     * @param fact The fact
     * @return true or false
     */
    private boolean addFact(Atom fact) {
        if (!knownFacts.contains(fact)) {
            var list = factsFor(fact.relation());
            list.add(fact);

            knownFacts.add(fact);
            return true;
        } else {
            return false;
        }
    }

    public List<Atom> factsFor(String relation) {
        return factMap.computeIfAbsent(relation,
            key -> new ArrayList<>());
    }


    public Set<Atom> getKnownFacts() {
        return Collections.unmodifiableSet(knownFacts);
    }

    //-------------------------------------------------------------------------
    // Inference

    /**
     * Executes the inference algorithm, computing all facts knowable
     * from the rules.
     */
    public void ponder() {
        knownFacts.clear();
        factMap.clear();
        baseFacts.forEach(this::addFact);

        int count = 0;
        boolean gotNewFact;
        do {
            gotNewFact = false;
            System.out.println("Iteration " + (++count) + ":");
            for (var rule : rules) {
                System.out.println("  Rule: " + rule);

                var iter = new TupleIterator(rule);
                while (iter.hasNext()) {
                    var tuple = iter.next();
                    var fact = matchRule(rule, tuple);

                    if (fact != null && addFact(fact)) {
                        System.out.println("    Fact: " + fact);
                        gotNewFact = true;
                    }
                }
            }
        } while (gotNewFact);
    }

    private Atom matchRule(Rule rule, Atom[] tuple) {
        // FIRST, match each pattern in the rule with a fact from
        // the tuple, binding variables from left to right.
        var bindings = new HashMap<Variable,Constant>();

        for (int i = 0; i < tuple.length; i++) {
            switch (rule.body().get(i)) {
                case BodyItem.Normal item -> {
                    if (!matchFact(bindings, item.atom(), tuple[i])) {
                        return null;
                    }
                }
                case BodyItem.Negated item -> {
                    // TODO: Leave this for now, just so the code runs.
                    if (!matchFact(bindings, item.atom(), tuple[i])) {
                        return null;
                    }
                }
            }
        }

        // NEXT, build the list of terms and return the new fact.
        var terms = new ArrayList<Term>();

        for (var term : rule.head().terms()) {
            switch (term) {
                case Constant c -> terms.add(c);
                case Variable v -> terms.add(bindings.get(v));
            }
        }

        return new Atom(rule.head().relation(), terms);
    }

    // Matches the fact against the rule pattern, return any
    // bound variables, or null on failure.
    private boolean matchFact(
        Map<Variable, Constant> bindings,
        Atom pattern,
        Atom fact
    ) {
//        System.out.println("      MatchFact " + bindings + " " + pattern +
//            " " + fact);
        var n = pattern.terms().size();
        if (fact.terms().size() != n) return false;

        for (var i = 0; i < pattern.terms().size(); i++) {
            var p = pattern.terms().get(i);
            var f = (Term.Constant)fact.terms().get(i);

            switch (p) {
                case Term.Variable v -> {
                    var bound = bindings.get(v);

                    if (bound == null) {
                        bindings.put(v, f);
                    } else if (!bound.equals(f)) {
                        return false;
                    }
                }
                case Term.Constant c -> {
                    if (!f.equals(c)) return false;
                }
            }
        }

        return true;
    }

    //-------------------------------------------------------------------------
    // TupleIterator

    private class TupleIterator {
        //---------------------------------------------------------------------
        // Instance Variables

        private final List<List<Atom>> inputs = new ArrayList<>();
        private Atom[] tuple = null;
        private final int total;
        private int next = 0;

        //---------------------------------------------------------------------
        // Constructor

        TupleIterator(Rule rule) {
            // FIRST, get the lists of facts in the order referenced in the
            // rule's body.
            int sum = 0;
            for (var item : rule.body()) {
                var relation = switch (item) {
                    case BodyItem.Normal a -> a.atom().relation();
                    case BodyItem.Negated na -> na.atom().relation();
                };
                var facts = factsFor(relation);

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
            tuple = new Atom[inputs.size()];
        }

        //---------------------------------------------------------------------
        // API

        public boolean hasNext() {
            return total != next;
        }

        public Atom[] next() {
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
