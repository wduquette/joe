package com.wjduquette.joe.nero;

import java.util.*;

public class DependencyGraph {
    //-------------------------------------------------------------------------
    // Instance Variables

    // Dependency Matrix:
    //
    // - dep[i][j] ==  0   -> i doesn't depend on j
    // - dep[i][j] ==  1   -> i depends on j with no negations
    // - dep[i][j] == -1   -> i depends on j with at least one negation
    private final int[][] dep;

    private final List<String> heads;

    // A list of strata; each stratum is a list of the rule heads in
    // that stratum.
    private final List<List<String>> strata;

    //-------------------------------------------------------------------------
    // Constructor

    public DependencyGraph(List<Rule> rules) {
        // FIRST, get the list of head predicates, and build the dependency
        // matrix.
        this.heads = getHeads(rules);
        this.dep = getDep(rules);

        // NEXT, compute the stratification.  The result is null if
        // the stratification failed.
        this.strata = stratify();
    }

    private List<String> getHeads(List<Rule> rules) {
        var result = new ArrayList<String>();
        for (var rule : rules) {
            var head = rule.head().relation();
            if (!result.contains(head)) {
                result.add(head);
            }
        }

        return result;
    }

    private int[][] getDep(List<Rule> rules) {
        int[][] mat = newMatrix(heads.size());

        for (var rule : rules) {
            var i = heads.indexOf(rule.head().relation());

            for (var b : rule.body()) {
                var j = heads.indexOf(b.relation());
                if (j == -1) continue;  // Skip non-head predicates

                if (b.negated()) {
                    mat[i][j] = -1;
                } else if (mat[i][j] == 0) {
                    mat[i][j] = 1;
                }
            }
        }

        return mat;
    }

    private int[][] newMatrix(int n) {
        var mat = new int[n][];
        for (var i = 0; i < n; i++) {
            mat[i] = new int[n];
        }
        return mat;
    }

    private List<List<String>> stratify() {
        var n = heads.size();
        var s = new int[n];

        // FIRST, compute the strata for each head.
        boolean change;
        do {
            change = false;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dep[i][j] > 0) {
                        if (s[i] < s[j]) {
                            s[i] = s[j];
                            change = true;
                        }
                    } else if (dep[i][j] < 0) {
                        if (s[i] <= s[j]) {
                            s[i] = s[j] + 1;

                            if (s[i] >= n) {
                                // Unstratified!
                                return null;
                            }

                            change = true;
                        }
                    }
                }
            }
        } while (change);

        var m = Arrays.stream(s).max().orElse(0) + 1;
        var result = new ArrayList<List<String>>();
        for (var i = 0; i < m; i++) {
            result.add(new ArrayList<>());
        }

        for (var i = 0; i < n; i++) {
            result.get(s[i]).add(heads.get(i));
        }

        return result;
    }

    //-------------------------------------------------------------------------
    // Public API

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
        return strata != null;
    }

    /**
     * Returns a list of the strata from stratum 0 to stratum N-1.
     * Each stratum is a list of the head relations in that stratum.
     * @return The list, or null if !isStratified.
     */
    public List<List<String>> strata() {
        return strata;
    }
}
