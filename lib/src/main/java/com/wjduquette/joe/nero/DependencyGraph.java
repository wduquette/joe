package com.wjduquette.joe.nero;

import java.util.*;

public class DependencyGraph {
    //-------------------------------------------------------------------------
    // Instance Variables


    private final List<Rule> rules;
    private final Map<String, List<Dependency>> map = new HashMap<>();
    private final HashSet<Pair> dependsOn = new HashSet<>();
    private final HashSet<Pair> negates = new HashSet<>();

    //-------------------------------------------------------------------------
    // Constructor

    public DependencyGraph(List<Rule> rules) {
        this.rules = rules;

        // FIRST, get the set of dependencies and negations.
        for (var rule : rules) {
            var head = rule.head().relation();
            for (var item : rule.body()) {
                var pair = new Pair(head, item.atom().relation());
                dependsOn.add(pair);
                if (item.isNegated()) {
                    negates.add(pair);
                }
            }
        }

        // NEXT, compute the dependency map
        for (var pair : dependsOn) {
            var neg = negates.contains(pair);
            var list = map.computeIfAbsent(pair.head, k -> new ArrayList<>());
            list.add(new Dependency(pair.head, pair.tail, neg));
        }
    }

    //-------------------------------------------------------------------------
    // API

    /**
     * Gets the relations that appear as the head in rules.
     * @return The head relations.
     */
    public Set<String> getHeadRelations() {
        return map.keySet();
    }

    /**
     * Gets the list of relations on which this relation depends.
     * @param relation the head relation
     * @return The list
     */
    public List<Dependency> getDependencies(String relation) {
        return map.get(relation);
    }

    /**
     * Gets a list of all dependencies.
     * @return The list
     */
    public List<Dependency> getDependencies() {
        var result = new ArrayList<Dependency>();
        map.values().forEach(result::addAll);
        return result;
    }

    /**
     * Returns true if the rule set is stratified, and false otherwise.
     *
     * <p>
     * A rule set is stratified if for relations p and q, if
     * p depends on not-q, q does not depend on p.
     * </p>
     * @return true or false
     */
    public boolean isStratified() {
        for (var pair : dependsOn) {
            if (negates.contains(pair) && dependsOn(pair.tail, pair.head)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true of the head relation depends on the tail relation,
     * and false otherwise.
     * @param head The head relation
     * @param tail The tail relation
     * @return true or false
     */
    public boolean dependsOn(String head, String tail) {
        return dependsOn.contains(new Pair(head, tail));
    }

    /**
     * Returns a list of the relations in stratified order, from
     * stratum 0 to stratum N.  Note: if !isStratified(), this might
     * not terminate!  We don't preserve the actual stratum
     * assignments, just the order in which to execute the rules.
     * @return The list
     */
    public List<String> stratify() {
        // TODO: Got to be a faster way to do this, with fewer collections.
        var heads = getHeadRelations();
        var strata = new HashMap<String, Integer>();

        // FIRST, put all heads into stratum 0
        heads.forEach(h -> strata.put(h, 0));

        boolean changed;
        var maxStratum = 0;
        do {
            changed = false;

            for (var rule : rules) {
                var head = rule.head().relation();
                var headStratum = strata.get(head);

                for (var item : rule.body()) {
                    var tail = item.atom().relation();
                    var tailStratum = strata.get(tail);

                    if (item.isNegated()) {
                        if (headStratum <= tailStratum) {
                            headStratum = tailStratum + 1;
                            strata.put(head, headStratum);
                            changed = true;
                        }
                    } else {
                        if (headStratum < tailStratum) {
                            headStratum = tailStratum;
                            strata.put(head, headStratum);
                            changed = true;
                        }
                    }
                }
                maxStratum = Math.max(headStratum, maxStratum);
            }
        } while (changed);

        // Produce sorted list.
        var byStratum = new HashMap<Integer, List<String>>();
        for (var e : strata.entrySet()) {
            var head = e.getKey();
            var stratum = e.getValue();
            var list = byStratum.computeIfAbsent(stratum, k->new ArrayList<>());
            list.add(head);
        }

        var result = new ArrayList<String>();
        for (var i = 0; i <= maxStratum; i++) {
            var list = byStratum.get(i);
            if (list != null) result.addAll(list);
        }

        return result;
    }

    //-------------------------------------------------------------------------
    // Helper Classes

    private record Pair(String head, String tail) {}

    /**
     * An entry in the dependency graph. The entry indicates that the `body`
     * relation appears in the body of at least one rule of which
     * `head` is the head relation.  If `negated` is true, then the
     * `body` relation is negated in at least one of those rules (not
     * necessarily in all of them).
     * @param head The head relation
     * @param tail The body relation
     * @param negated true or false
     */
    public record Dependency(String head, String tail, boolean negated) {}

}
