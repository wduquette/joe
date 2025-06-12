package com.wjduquette.joe.patterns;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeValue;
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
        /**
         * Gets the value of the constant with the given ID.
         * @param id The ID
         * @return the value
         */
        Object get(int id);
    }

    /**
     * A function to bind a binding variable to its value given
     * the binding ID.
     */
    public interface Binder {
        /**
         * Provides the binding name/value pair to the client.
         * @param name The binding name
         * @param value The bound value
         */
        void bind(String name, Object value);
    }

    /**
     * Matches the pattern to the target value, binding variables in the
     * pattern to the matching sub-elements of the value.  Returns true
     * on success and false on failure.
     *
     * <p>The binder function is called once for each such variable;
     * variables are bound in the order in which they appear in the pattern.
     * </p>
     * @param joe The interpreter
     * @param pattern The pattern
     * @param value The target value
     * @param getter The Constant getter function
     * @param binder The binder function
     * @return true or false.
     */
    public static boolean bind(
        Joe joe,
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
                binder.bind(p.name(), value);
                yield true;
            }
            case Pattern.PatternBinding p -> {
                binder.bind(p.name(), value);
                yield bind(joe, p.subpattern(), value, getter, binder);
            }

            case Pattern.ListPattern p -> {
                // FIRST, check type and shape
                var size = p.patterns().size();

                if (!(value instanceof List<?> list)) yield false;
                if (size > list.size()) yield false;
                if (p.tailVar() == null && size < list.size()) yield false;

                // NEXT, match items
                for (var i = 0; i < size; i++) {
                    if (!bind(joe, p.patterns().get(i), list.get(i), getter, binder)) {
                        yield false;
                    }
                }

                // NEXT, bind the tail to the tailId
                if (p.tailVar() != null) {
                    var tail = list.subList(size, list.size());
                    binder.bind(p.tailVar(), tail);
                }

                // FINALLY, match succeeds.
                yield true;
            }

            case Pattern.MapPattern p -> {
                if (value instanceof Map<?,?> map) {
                    // NEXT, match keys and values
                    for (var e : p.patterns().entrySet()) {
                        var key = getter.get(e.getKey().id());
                        if (!map.containsKey(key)) yield false;

                        var item = map.get(key);
                        if (!bind(joe, e.getValue(), item, getter, binder)) {
                            yield false;
                        }
                    }

                    // FINALLY, match succeeds
                    yield true;
                } else {
                    var obj = joe.getJoeValue(value);

                    for (var e : p.patterns().entrySet()) {
                        var field = key2field(getter.get(e.getKey().id()));
                        if (!obj.hasField(field)) yield false;

                        if (!bind(joe, e.getValue(), obj.get(field), getter, binder)) {
                            yield false;
                        }
                    }

                    // FINALLY, match succeeds
                    yield true;
                }
            }

            case Pattern.NamedFieldPattern p -> {
                var obj = joe.getJoeValue(value);
                if (!hasType(obj, p.typeName())) yield false;

                for (var e : p.fieldMap().entrySet()) {
                    var field = e.getKey();
                    if (!obj.hasField(field)) yield false;

                    if (!bind(joe, e.getValue(), obj.get(field), getter, binder)) {
                        yield false;
                    }
                }

                // FINALLY, match succeeds
                yield true;
            }

            case Pattern.OrderedFieldPattern p -> {
                // FIRST, check type and shape.  The value must be
                // a JoeValue of a record type; there must be one
                // pattern for each field; and each pattern must match
                // the corresponding field.
                var obj = joe.getJoeValue(value);
                if (!obj.hasOrderedFields()) yield false;
                if (!obj.type().name().equals(p.typeName())) yield false;

                var fields = obj.getFieldNames();
                var size = p.patterns().size();
                if (fields.size() != size) yield false;

                var list = fields.stream().map(obj::get).toList();

                // NEXT, match items
                for (var i = 0; i < size; i++) {
                    if (!bind(joe, p.patterns().get(i), list.get(i), getter, binder)) {
                        yield false;
                    }
                }

                // FINALLY, match succeeds.
                yield true;
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
    private static boolean hasType(JoeValue obj, String typeName) {
        var got = obj.type();

        while (got != null) {
            if (got.name().equals(typeName)) return true;

            got = got.supertype();
        }
        return false;
    }
}
