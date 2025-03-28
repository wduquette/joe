package com.wjduquette.joe.nero;

import java.util.*;

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
    private final List<Fact> baseFacts = new ArrayList<>();

    // The current set of known facts.
    private final Set<Fact> knownFacts = new HashSet<>();

    // Facts by relation
    private final Map<String, List<Fact>> factMap = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    public RuleSet(List<Rule> rules, List<Fact> baseFacts) {
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
    private boolean addFact(Fact fact) {
        if (!knownFacts.contains(fact)) {
            var list = factsFor(fact.relation());
            list.add(fact);

            knownFacts.add(fact);
            return true;
        } else {
            return false;
        }
    }

    public List<Fact> factsFor(String relation) {
        return factMap.computeIfAbsent(relation,
            key -> new ArrayList<>());
    }


    public Set<Fact> getKnownFacts() {
        return Collections.unmodifiableSet(knownFacts);
    }

    //-------------------------------------------------------------------------
    // Inference

    /**
     * Executes the inference algorithm, computing all facts knowable
     * from the rules.
     */
    public void ponder() {
        var gotNewFact = false;
        knownFacts.clear();
        knownFacts.addAll(baseFacts);

        do {
            for (var rule : rules) {
                var iter = new TupleIterator(rule);
                while (iter.hasNext()) {
                    var fact = matchRule(rule, iter.next());

                    if (fact != null && !knownFacts.contains(fact)) {
                        addFact(fact);
                        gotNewFact = true;
                    }
                }
            }
        } while (gotNewFact);
    }

    private Fact matchRule(Rule rule, Fact[] tuple) {
        var terms = new ArrayList<Term>();

        // TODO
        for (int i = 0; i < tuple.length; i++) {

        }
        return null;
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
            for (var item : rule.body()) {
                var facts = factsFor(item.relation());

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

            for (var i = 0; i < inputs.size(); i++) {
                var input = inputs.get(i);
                tuple[i] = input.get(n % input.size());
                n /= input.size();
            }

            return tuple;
        }
    }
}
