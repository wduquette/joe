package com.wjduquette.joe.patterns;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeValue;
import com.wjduquette.joe.Keyword;
import com.wjduquette.joe.nero.Fact;

import java.util.*;

/**
 * Instances of Matcher can match a pattern against a target value,
 * binding variable IDs to sub-values.
 */
public class Matcher {
    private Matcher() {} // Not instantiable

    /**
     * A function to retrieve an Expression pattern's value given its
     * ID.
     */
    public interface ExpressionGetter {
        /**
         * Gets the value of the Expression with the given ID.
         * @param id The ID
         * @return the value
         */
        Object get(int id);
    }

    /**
     * Matches the pattern to the target value, binding variables in the
     * pattern to the matching sub-elements of the value.  Returns the
     * bindings on success and null on failure.
     * @param joe The interpreter
     * @param pattern The pattern
     * @param value The target value
     * @param getter The Constant getter function
     * @return The bindings.
     */
    public static LinkedHashMap<String,Object> bind(
        Joe joe,
        Pattern pattern,
        Object value,
        ExpressionGetter getter
    ) {
        // Use a LinkedHashMap to ensure that values are ordered!
        var bindings = new LinkedHashMap<String,Object>();

        if (doBind(joe, pattern, value, getter, bindings)) {
            return bindings;
        } else {
            return null;
        }
    }

    private static boolean doBind(
        Joe joe,
        Pattern pattern,
        Object value,
        ExpressionGetter getter,
        Map<String,Object> bindings
    ) {
        return switch (pattern) {
            case Pattern.Constant p ->
                matchConstant(p.value(), value);

            case Pattern.Expression p ->
                matchConstant(getter.get(p.id()), value);

            case Pattern.ListPattern p -> {
                // FIRST, check type and shape
                var size = p.patterns().size();

                if (!(value instanceof List<?> list)) yield false;
                if (size > list.size()) yield false;
                if (p.tailVar() == null && size < list.size()) yield false;

                // NEXT, match items
                for (var i = 0; i < size; i++) {
                    if (!doBind(joe, p.patterns().get(i), list.get(i), getter, bindings)) {
                        yield false;
                    }
                }

                // NEXT, bind the tail to the tailId
                if (p.tailVar() != null) {
                    var tail = list.subList(size, list.size());
                    if (bindings.containsKey(p.tailVar()) &&
                        !bindings.get(p.tailVar()).equals(tail))
                    {
                        yield false;
                    }
                    bindings.put(p.tailVar(), tail);
                }

                // FINALLY, match succeeds.
                yield true;
            }

            case Pattern.MapPattern p -> {
                if (value instanceof Map<?,?> map) {
                    // NEXT, match keys and values
                    for (var e : p.patterns().entrySet()) {
                        var key = getConstant(e.getKey(), getter);
                        if (!map.containsKey(key)) yield false;

                        var item = map.get(key);
                        if (!doBind(joe, e.getValue(), item, getter, bindings)) {
                            yield false;
                        }
                    }

                    // FINALLY, match succeeds
                    yield true;
                }

                yield false;
            }

            case Pattern.NamedFieldPattern p -> {
                Fact fact;

                if (value instanceof Fact f) {
                    fact = f;
                } else {
                    var obj = joe.getJoeValue(value);
                    if (!obj.isFact()) yield false;

                    fact = obj.toFact();
                    if (!p.typeName().equals(fact.relation()) &&
                        !hasType(obj, p.typeName())
                    ) yield false;
                }

                var map = fact.getFieldMap();

                for (var e : p.fieldMap().entrySet()) {
                    var field = e.getKey();
                    if (!map.containsKey(field)) yield false;

                    if (!doBind(joe, e.getValue(), map.get(field), getter, bindings)) {
                        yield false;
                    }
                }

                // FINALLY, match succeeds
                yield true;
            }

            case Pattern.OrderedFieldPattern p -> {
                Fact fact;
                var jv = joe.getJoeValue(value);

                if (value instanceof Fact f) {
                    // It's a `Fact` object; use it as is.
                    fact = f;
                    if (!fact.relation().equals(p.typeName())) yield false;
                } else if (jv.isFact()) {
                    // It's convertible to a fact object; verify that the
                    // requested name is the object's relation, type,
                    // or a supertype.
                    fact = jv.toFact();

                    if (!p.typeName().equals(fact.relation()) &&
                        !hasType(jv, p.typeName())
                    ) yield false;
                } else {
                    // It's not convertible to a fact, i.e., it has no
                    // fields. If the pattern has no field patterns,
                    // check the name; otherwise it fails.

                    yield p.patterns().isEmpty() && hasType(jv, p.typeName());
                }

                // At this point we know:
                //
                // - The type name/relation matches.
                // - The `fact` variable makes the fields visible.

                // Check for field patterns.
                if (p.patterns().isEmpty()) yield true;

                // Check the fields; the fact must be ordered.
                if (!fact.isOrdered()) yield false;

                var fields = fact.getFields();
                var size = p.patterns().size();
                if (fields.size() != size) yield false;

                // NEXT, match items
                for (var i = 0; i < size; i++) {
                    if (!doBind(joe, p.patterns().get(i), fields.get(i), getter, bindings)) {
                        yield false;
                    }
                }

                // FINALLY, match succeeds.
                yield true;
            }

            case Pattern.PatternBinding p -> {
                if (bindings.containsKey(p.name()) &&
                    !bindings.get(p.name()).equals(value)
                ) {
                    yield false;
                }
                bindings.put(p.name(), value);
                yield doBind(joe, p.subpattern(), value, getter, bindings);
            }

            case Pattern.TypeName p -> {
                Fact fact;
                var jv = joe.getJoeValue(value);

                if (value instanceof Fact f) {
                    // It's a `Fact` object; use it as is.
                    fact = f;
                    yield p.typeName().equals(fact.relation())
                        || p.typeName().equals("Fact");
                } else if (jv.isFact()) {
                    // It's convertible to a fact object; verify that the
                    // requested name is the object's relation, type,
                    // or a supertype.
                    fact = jv.toFact();
                    yield fact.relation().equals(p.typeName())
                        || hasType(jv, p.typeName());
                } else {
                    yield hasType(jv, p.typeName());
                }
            }

            case Pattern.ValueBinding p -> {
                if (bindings.containsKey(p.name()) &&
                    !bindings.get(p.name()).equals(value)
                ) {
                    yield false;
                }
                bindings.put(p.name(), value);
                yield true;
            }

            case Pattern.Wildcard ignored
                -> true;
        };
    }

    private static Object getConstant(Pattern pattern, ExpressionGetter getter) {
        return switch (pattern) {
            case Pattern.Constant p -> p.value();
            case Pattern.Expression p -> getter.get(p.id());
            default -> throw new IllegalStateException(
                "Invalid map key pattern: '" + pattern + "'.");
        };
    }

    private static boolean matchConstant(Object constant, Object value) {
        if (Objects.equals(constant, value)) return true;
        return constant instanceof Keyword k
            && value instanceof Enum<?> e
            && k.name().equalsIgnoreCase(e.name());
    }

    // Determines whether the type name is the name of the object's
    // type or the name of one of its supertypes.
    private static boolean hasType(JoeValue obj, String typeName) {
        var got = obj.type();

        while (got != null) {
            if (got.name().equals(typeName)) return true;

            got = got.supertype();
        }
        return false;
    }
}
