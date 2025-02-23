package com.wjduquette.joe.binder;

import com.wjduquette.joe.JoeList;
import com.wjduquette.joe.JoeMap;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Instances of Binder can match a pattern against a target value,
 * binding variable IDs to sub-values.  The bindings can be returned
 * as a Java {@code Map}, or can be handled immediately via a
 * callback.
 */
public class Binder {
    private Binder() {} // Not instantiable

    /**
     * Given a pattern, returns a list of its binding variable IDs,
     * in the order in which they appear in the pattern.
     * @param pattern The pattern
     * @return The list
     */
    public static List<Object> getVarIds(Pattern pattern) {
        var ids = new ArrayList<>();
        findVarIds(pattern, ids);
        return ids;
    }

    private static void findVarIds(Pattern pattern, List<Object> ids) {
        switch (pattern) {
            case Pattern.ListPattern p -> {
                for (var item : p.patterns()) {
                    findVarIds(item, ids);
                }
                if (p.tailId() != null) {
                    ids.add(p.tailId());
                }
            }
            case Pattern.MapPattern p -> {
                for (var vp : p.patterns().values()) {
                    findVarIds(vp, ids);
                }
            }
            case Pattern.SubPatternBinding p -> {
                ids.add(p.varId());
                findVarIds(p.pattern(), ids);
            }
            case Pattern.ValueBinding p -> ids.add(p.varId());
            default -> {}
        }
    }

    /**
     * Matches the pattern to the target value, binding variables in the
     * pattern to the matching sub-elements of the value.
     * Returns a map of the bindings on success and null on failure.
     * @param pattern The pattern
     * @param value The target value
     * @return The map or null.
     */
    public static Map<Object,Object> bind(
        Pattern pattern,
        Object value
    ) {
        var map = new LinkedHashMap<>();
        if (bind(pattern, value, map::put)) {
            return map;
        } else {
            return null;
        }
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
     * @param binder The binder function
     * @return true or false.
     */
    public static boolean bind(
        Pattern pattern,
        Object value,
        BiConsumer<Object,Object> binder
    ) {
        return switch (pattern) {
            case Pattern.ListPattern p -> {
                // FIRST, check type and shape
                var size = p.patterns().size();

                if (!(value instanceof JoeList list)) yield false;
                if (size > list.size()) yield false;
                if (p.tailId() == null && size < list.size()) yield false;

                // NEXT, match items
                for (var i = 0; i < size; i++) {
                    if (!bind(p.patterns().get(i), list.get(i), binder)) {
                        yield false;
                    }
                }

                // NEXT, bind the tail to the tailId
                if (p.tailId() != null) {
                    var tail = list.subList(size, list.size());
                    binder.accept(p.tailId(), tail);
                }

                // FINALLY, match succeeds.
                yield true;
            }

            case Pattern.MapPattern p -> {
                // FIRST, check type.
                if (!(value instanceof JoeMap map)) yield false;

                // NEXT, match keys and values
                for (var e : p.patterns().entrySet()) {
                    if (!map.containsKey(e.getKey())) yield false;
                    var item = map.get(e.getKey());
                    if (!bind(e.getValue(), item, binder)) yield false;
                }

                // FINALLY, match succeeds
                yield true;
            }

            case Pattern.SubPatternBinding p -> {
                binder.accept(p.varId(), value);
                yield bind(p.pattern(), value, binder);
            }

            case Pattern.ValueBinding p -> {
                binder.accept(p.varId(), value);
                yield true;
            }

            case Pattern.ValuePattern p -> Objects.equals(p.value(), value);

            case Pattern.Wildcard ignored -> true;
        };
    }
}
