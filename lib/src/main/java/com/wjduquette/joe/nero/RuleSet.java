package com.wjduquette.joe.nero;

import com.wjduquette.joe.JoeError;

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

    // Map from head relation to rules with that head.
    private final Map<String,List<Rule>> ruleMap = new HashMap<>();

    // Head relations by stratum.
    private final List<List<String>> strata;

    // Facts as read from the Nero program.
    private final List<Atom> baseFacts = new ArrayList<>();

    // The current set of known facts.
    private final Set<Atom> knownFacts = new HashSet<>();

    // Facts by relation
    private final Map<String, List<Atom>> factMap = new HashMap<>();

    // A map of all atoms queried as part of negated body items,
    // the value is true if such an atom was found, and false.
    private final Map<Atom, Boolean> found = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    public RuleSet(List<Rule> rules, List<Atom> baseFacts) {
        // FIRST, analyze the rule set
        var graph = new Graph(rules);
        if (!graph.isStratified()) {
            throw new JoeError("Rule set is not stratified.");
        }

        this.strata = graph.strata();

        // NEXT, Categorize the rules by head relation
        for (var rule : rules) {
            var head = rule.head().relation();
            var list = ruleMap.computeIfAbsent(head, k -> new ArrayList<>());
            list.add(rule);
        }

        // NEXT, save the base facts.
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

        for (var i = 0; i < strata.size(); i++) {
            ponder(i, strata.get(i));
        }
    }

    private void ponder(int stratum, List<String> heads) {
        int count = 0;
        boolean gotNewFact;
        do {
            gotNewFact = false;
            System.out.println("Iteration " + stratum + "." + (++count) + ":");
            for (var head : heads) {
                for (var rule : ruleMap.get(head)) {
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
                    // FIRST, get the atom with bindings.
                    var bound = bindAtom(bindings, item.atom());

                    // NEXT, have we already computed this?
                    var flag = found.get(bound);

                    if (flag == null) {
                        flag = isKnown(bound);
                        found.put(bound, flag);
                    }

                    if (flag) {
                        // We found a fact; the rule fails.
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

    // Return a copy of the atom replacing variables with bound
    // constants.  The result might still have variables.
    private Atom bindAtom(Map<Variable, Constant> bindings, Atom atom) {
        var terms = new ArrayList<Term>();

        for (var i = 0; i < atom.terms().size(); i++) {
            // If the term is variable and a binding is available,
            // replace the variable with the bound constant.
            switch (atom.terms().get(i)) {
                case Constant c -> terms.add(c);
                case Variable v -> {
                    var value = bindings.get(v);
                    if (value != null) {
                        terms.add(value);
                    } else {
                        terms.add(v);
                    }
                }
            }
        }

        return new Atom(atom.relation(), terms);
    }

    // Looks through the facts to see if we have a known fact that
    // matches this atom's constant terms.  This is used to implement
    // "not" items.
    private boolean isKnown(Atom query) {
        for (var fact : factsFor(query.relation())) {
            if (matches(fact, query)) {
                return true;
            }
        }
        return false;
    }

    // Returns true if every constant in the query matches the
    // corresponding term in the fact.
    private boolean matches(Atom fact, Atom query) {
        if (!fact.relation().equals(query.relation())) {
            return false;
        }

        for (var i = 0; i < query.terms().size(); i++) {
            switch (query.terms().get(i)) {
                case Constant c -> {
                    if (!fact.terms().get(i).equals(c)) {
                        return false;
                    }
                }
                case Variable ignored -> {}
            }
        }

        return true;
    }

    // Tries to match the fact against the rule pattern given the bindings,
    // updating the bindings where the rule's pattern has a free variable.
    // Returns true on success and false.
    private boolean matchFact(
        Map<Variable, Constant> bindings,
        Atom pattern,
        Atom fact
    ) {
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
