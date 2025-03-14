package com.wjduquette.joe.patterns;

import com.wjduquette.joe.JoeObject;
import com.wjduquette.joe.Keyword;

import java.util.*;

/**
 * Instances of Matcher can match a pattern against a target value,
 * binding variable IDs to sub-values.
 */
public class Matcher {
    private Matcher() {} // Not instantiable

    /**
     * A function to retrieve a pattern constant's value given its
     * ID.
     */
    public interface ConstantGetter {
        Object get(int id);
    }

    /**
     * A function to bind a binding variable to its value given
     * the binding ID.
     */
    public interface Binder {
        void bind(int id, Object value);
    }

    /**
     * Matches the pattern to the target value, binding variables in the
     * pattern to the matching sub-elements of the value.  Returns true
     * on success and false on failure.
     *
     * <p>The binder function is called once for each such variable;
     * variables are bound in the order in which they appear in the pattern.
     * </p>
     * @param pattern The pattern
     * @param value The target value
     * @param getter The Constant getter function
     * @param binder The binder function
     * @return true or false.
     */
    public static boolean bind(
        Pattern pattern,
        Object value,
        ConstantGetter getter,
        Binder binder
    ) {
        return switch (pattern) {
            case Pattern.Constant p ->
                Objects.equals(getter.get(p.id()), value);
            case Pattern.Wildcard ignored
                -> true;
            case Pattern.ValueBinding p -> {
                binder.bind(p.id(), value);
                yield true;
            }
            case Pattern.PatternBinding p -> {
                binder.bind(p.id(), value);
                yield bind(p.subpattern(), value, getter, binder);
            }

            case Pattern.ListPattern p -> {
                // FIRST, check type and shape
                var size = p.patterns().size();

                if (!(value instanceof List<?> list)) yield false;
                if (size > list.size()) yield false;
                if (p.tailId() == null && size < list.size()) yield false;

                // NEXT, match items
                for (var i = 0; i < size; i++) {
                    if (!bind(p.patterns().get(i), list.get(i), getter, binder)) {
                        yield false;
                    }
                }

                // NEXT, bind the tail to the tailId
                if (p.tailId() != null) {
                    var tail = list.subList(size, list.size());
                    binder.bind(p.tailId(), tail);
                }

                // FINALLY, match succeeds.
                yield true;
            }

            case Pattern.MapPattern p -> switch (value) {
                case Map<?,?> map -> {
                    // NEXT, match keys and values
                    for (var e : p.patterns().entrySet()) {
                        var key = getter.get(e.getKey().id());
                        if (!map.containsKey(key)) yield false;

                        var item = map.get(key);
                        if (!bind(e.getValue(), item, getter, binder)) {
                            yield false;
                        }
                    }

                    // FINALLY, match succeeds
                    yield true;
                }
                case JoeObject obj -> {
                    for (var e : p.patterns().entrySet()) {
                        var field = key2field(getter.get(e.getKey().id()));
                        if (!obj.hasField(field)) yield false;

                        if (!bind(e.getValue(), obj.get(field), getter, binder)) {
                            yield false;
                        }
                    }

                    // FINALLY, match succeeds
                    yield true;

                }
                default -> false;
            };

            case Pattern.InstancePattern p -> {
                if (!(value instanceof JoeObject obj)) yield false;
                if (!hasType(obj, p.typeName())) yield false;

                yield bind(p.fieldMap(), obj, getter, binder);
            }
        };
    }

    private static String key2field(Object key) {
        if (key instanceof String s) {
            return s;
        } else if (key instanceof Keyword k) {
            return k.name();
        } else {
            return null;
        }
    }

    // Determines whether the type name is the name of the object's
    // type or the name of one of its supertypes.
    private static boolean hasType(JoeObject obj, String typeName) {
        // TODO: Match on supertypes.
        return obj.typeName().equals(typeName);
    }
}
