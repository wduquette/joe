package com.wjduquette.joe.nero;

import java.util.*;

public class Graph {
    //-------------------------------------------------------------------------
    // Instance Variables

    // List of head relations by name.
    private final List<String> heads = new ArrayList<>();

    // Dependency Matrix:
    //
    // - dep[i][j] ==  0   -> i doesn't depend on j
    // - dep[i][j] ==  1   -> i depends on j with no negations
    // - dep[i][j] == -1   -> i depends on j with at least one negation
    private final int[][] dep;

    //-------------------------------------------------------------------------
    // Constructor

    public Graph(List<Rule> rules) {
        // FIRST, get the set and list of heads.
        var headSet = new HashSet<String>();
        for (var rule : rules) {
            var head = rule.head().relation();
            if (headSet.add(head)) {
                heads.add(head);
            }
        }

        // NEXT, allocate the dependency matrix
        dep = new int[heads.size()][];
        for (var i = 0; i < heads.size(); i++) {
            dep[i] = new int[heads.size()];
        }

        // NEXT, scan the rules.
        for (var rule : rules) {
            var i = heads.indexOf(rule.head().relation());

            for (var item : rule.body()) {
                var j = heads.indexOf(item.atom().relation());
                if (j == -1) continue;  // Skip non-heads

                if (item.isNegated()) {
                    dep[i][j] = -1;
                } else if (dep[i][j] == 0) {
                    dep[i][j] = 1;
                }
            }
        }
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
        var n = heads.size();

        for (var i = 0; i < n; i++) {
            for (var j = 0; j < n; j++) {
                if (dep[i][j] < 0 && dep[j][i] != 0) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returns a list of the relations in stratified order, from
     * stratum 0 to stratum N.  Note: if !isStratified(), this might
     * not terminate!  We don't preserve the actual stratum
     * assignments, just the order in which to execute the rules.
     * @return The list
     */
    public List<String> stratify() {
        var n = heads.size();
        var strata = new int[n];

        // FIRST, compute the strata for each head.
        boolean change;
        do {
            change = false;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dep[i][j] > 0) {
                        if (strata[i] < strata[j]) {
                            strata[i] = strata[j];
                            change = true;
                        }
                    } else if (dep[i][j] < 0) {
                        if (strata[i] <= strata[j]) {
                            strata[i] = strata[j] + 1;
                            change = true;
                        }
                    }
                }
            }
        } while (change);


        // NEXT, order heads by strata
        heads.sort((h, t) -> {
            var i = heads.indexOf(h);
            var j = heads.indexOf(t);
            return Integer.compare(strata[i], strata[j]);
        });

        return heads;
    }

    //-------------------------------------------------------------------------
    // Helper Classes


}
