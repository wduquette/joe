package com.wjduquette.joe.patterns;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A binding Pattern is used to destructure a complex data structure and
 * bind values within it to Joe variables.  As such, Pattern is an
 * algebraic data type (ADT) used to compose complex patterns in ways
 * that match data structures.
 *
 * <p>Several of the Pattern variants have a variable ID field, which
 * identifies a binding variable to receive the matched value.  The
 * variable ID is an arbitrary Java object, as the {@link Matcher} is
 * independent of the language engine, and the type of the ID will differ
 * from engine to engine, and possible from differ for different kinds of
 * variable.</p>
 */
public sealed interface Pattern permits
    Pattern.Constant,
    Pattern.Wildcard,
    Pattern.PatternBinding,
    Pattern.ValueBinding,
    Pattern.ListPattern,
    Pattern.MapPattern
{
    /**
     * A pattern that requires the target value to be exactly equal to a
     * constant value.  Constants are passed out-of-band, based on an
     * integer ID, because the details depend on the language engine.
     * @param id The constant's ID
     */
    record Constant(int id) implements Pattern {
        /**
         * Returns "$id" as the string representation.
         * @return The string
         */
        @Override public String toString() {
            return "$" + id;
        }
    }

    /**
     * A pattern that matches any value at all.  A wildcard's name is an
     * identifier with an initial underscore.  A single underscore,
     * "{@code _}", is common, but longer names can be used to improve
     * readability.  The name has no effect at all on the pattern
     * match.
     * @param name The wildcard name.
     */
    record Wildcard(String name) implements Pattern {
        /**
         * Returns the wildcard's name as the string representation.
         * @return The string
         */
        @Override public String toString() {
            return name;
        }
    }

    /**
     * A pattern that binds the matching target value to the binding variable
     * with the given ID.  Binding variables are identified using integer
     * IDs because the details depend on the language engine.
     * @param id A binding variable ID
     */
    record ValueBinding(int id) implements Pattern {
        /**
         * Returns "?id" as the string representation.
         * @return The string
         */
        @Override public String toString() {
            return "?" + id;
        }
    }

    /**
     * A pattern that matches a subpattern and binds its value to a binding
     * variable.
     * @param id A binding variable Id
     * @param subpattern The subpattern to match.
     */
    record PatternBinding(int id, Pattern subpattern) implements Pattern {
        /**
         * Returns "?id" as the string representation.
         * @return The string
         */
        @Override public String toString() {
            return "?" + id + " = " + subpattern;
        }
    }

    /**
     * A pattern that matches a target
     * {@link com.wjduquette.joe.JoeList}.  The patterns in the
     * {@code patterns} list must match the corresponding items in the
     * target list.  The overall match fails if they do not, or if there are
     * too few items in the target list.
     *
     * <p>If the {@code tailId} is null then the match will also fail if
     * there are excess items in the target list.  Otherwise,
     * {@code tailId} will is a binding ID, and a list containing
     * the excess items will be bound to that ID.
     * @param patterns The item patterns
     * @param tailId The ID for the tail binding, or null.
     */
    record ListPattern(List<Pattern> patterns, Integer tailId)
        implements Pattern
    {
        @Override public String toString() {
            var list = patterns.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
            return "[" + list +
                (tailId != null ? " | ?" + tailId : "") +
                "]";
        }
    }

    /**
     * A pattern that matches a target
     * {@link com.wjduquette.joe.JoeMap}.  Each key constant in the
     * {@code patterns} map must exist as a key in the target map, and
     * each key's value pattern in the {@code patterns} map must match
     * the key's value in the target map.
     * @param patterns The key constants and value patterns
     */
    record MapPattern(Map<Constant,Pattern> patterns)
        implements Pattern
    {
        @Override public String toString() {
            var map = patterns.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));
            return "{" + map + "}";
        }
    }
}
