package com.wjduquette.joe.nero;

import com.wjduquette.joe.types.ListValue;
import com.wjduquette.joe.types.MapValue;
import com.wjduquette.joe.types.SetValue;
import com.wjduquette.joe.util.Bindings;

import java.util.HashSet;
import java.util.Set;

/** A Term in a Nero {@link Atom}. */
public sealed interface Term permits
    Aggregate,
    Constant,
    ListTerm,
    MapTerm,
    SetTerm,
    Variable,
    Wildcard
{
    /**
     * Computes the value of the term as used in a rule head or
     * axiom, given the bindings.
     * @param bindings The bindings
     * @return The value
     */
    static Object toValue(Term term, Bindings bindings) {
        return switch (term) {
            case Aggregate ignored -> bindings.get(RuleEngine.AGGREGATE);
            case Constant c -> c.value();
            case ListTerm lt -> {
                var result = new ListValue();
                for (var t : lt.terms()) {
                    result.add(Term.toValue(t, bindings));
                }
                yield result;
            }
            case MapTerm m -> {
                var result = new MapValue();
                assert m.pairs().size() % 2 == 0;
                for (var i = 0; i < m.pairs().size(); i += 2) {
                    result.put(
                        Term.toValue(m.pairs().get(i), bindings),
                        Term.toValue(m.pairs().get(i+1), bindings));
                }
                yield result;
            }
            case SetTerm s -> {
                var result = new SetValue();
                for (var t : s.terms()) {
                    result.add(Term.toValue(t, bindings));
                }
                yield result;
            }
            case Variable v -> bindings.get(v.name());
            default -> throw new UnsupportedOperationException(
                "toValue is unsupported for body term: " +
                    term.getClass().getSimpleName() + " '" +
                    term + "'.");
        };
    }

    /**
     * Gets the term's variable names.
     * @param term The term
     * @return the set of names.
     */
    static Set<String> getVariableNames(Term term) {
        return switch (term) {
            case Aggregate a -> new HashSet<>(a.names());
            case Variable v -> Set.of(v.name());
            default -> Set.of();
        };
    }
}
