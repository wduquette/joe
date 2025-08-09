package com.wjduquette.joe.nero;

import java.util.ArrayList;
import java.util.List;

/**
 * A map-literal term in a Nero axiom or rule head.
 * The keys and values are passed as a flat list of key/value pairs.
 * @param pairs The literal's terms.
 */
public record MapTerm(List<Term> pairs)
    implements Term
{
    @Override
    public String toString() {
        if (pairs.isEmpty()) return "{:}";

        assert pairs.size() % 2 == 0;

        var list = new ArrayList<String>();
        for (var i = 0; i < pairs.size(); i += 2) {
            list.add(pairs.get(i) + ": " + pairs.get(i + 1));
        }
        return "{" + String.join(", ", list) + "}";
    }
}
