package com.wjduquette.joe.patterns;

import com.wjduquette.joe.JoeValue;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    Pattern.Expression,
    Pattern.ListPattern,
    Pattern.MapPattern,
    Pattern.NamedFieldPattern,
    Pattern.OrderedFieldPattern,
    Pattern.PatternBinding,
    Pattern.TypeName,
    Pattern.ValueBinding,
    Pattern.Wildcard
{
    /**
     * A pattern that requires the target value to be exactly equal to a
     * literal constant.
     * @param value The constant's value
     */
    record Constant(Object value) implements Pattern {
        /**
         * Returns the value's string representation.
         * @return The string
         */
        @Override public String toString() {
            return Objects.toString(value);
        }
    }

    /**
     * A pattern that requires the target value to be exactly equal to a
     * computed value.  Expressions are passed out-of-band, based on an
     * integer ID, because the actual expression needs to be computed by
     * the language engine and the result provided to the matching
     * algorithm.
     * @param id The constant's ID
     */
    record Expression(int id) implements Pattern {
        /**
         * Returns "$id" as the string representation.
         * @return The string
         */
        @Override public String toString() {
            return "$" + id;
        }
    }

    /**
     * A pattern that matches a target
     * {@link com.wjduquette.joe.JoeList}.  The patterns in the
     * {@code patterns} list must match the corresponding items in the
     * target list.  The overall match fails if they do not, or if there are
     * too few items in the target list.
     *
     * <p>If the {@code tailVar} is null then the match will also fail if
     * there are excess items in the target list.  Otherwise,
     * {@code tailVar} will be bound to a list containing
     * the excess items.
     * @param patterns The item patterns
     * @param tailVar The name for the tail binding, or null.
     */
    record ListPattern(List<Pattern> patterns, String tailVar)
        implements Pattern
    {
        @Override public String toString() {
            var list = patterns.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
            return "[" + list +
                (tailVar != null ? " | ?" + tailVar : "") +
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
    record MapPattern(Map<Pattern,Pattern> patterns)
        implements Pattern
    {
        @Override public String toString() {
            if (patterns.isEmpty()) return "{:}";

            var map = patterns.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));
            return "{" + map + "}";
        }
    }

    /**
     * A pattern that matches a target {@link JoeValue} on its type
     * name and fields. The given type name must match the name of the
     * target value's Joe type or one of its supertypes.  The field
     * The keys in the field map must be existing field names, and
     * the patterns in the field map must match the relevant values.
     * @param typeName The name of the desired type.
     * @param fieldMap The field names and value patterns
     */
    record NamedFieldPattern(String typeName, Map<String,Pattern> fieldMap)
        implements Pattern
    {
        @Override public String toString() {
            var map = fieldMap.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));
            return typeName + "(" + map + ")";
        }
    }

    /**
     * A pattern that matches an instance of a target
     * Joe value that has ordered fields. The given type name must
     * match the target object's type name.  The field match is done
     * field by field in the manner of ListPattern.
     * @param typeName The name of the desired type.
     * @param patterns The field value patterns
     */
    record OrderedFieldPattern(String typeName, List<Pattern> patterns)
        implements Pattern
    {
        @Override public String toString() {
            var list = patterns.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
            return typeName + "(" + list + ")";
        }
    }

    /**
     * A pattern that matches a subpattern and binds its value to a binding
     * variable.
     * @param name A binding variable name
     * @param subpattern The subpattern to match.
     */
    record PatternBinding(String name, Pattern subpattern) implements Pattern {
        /**
         * Returns "?id" as the string representation.
         * @return The string
         */
        @Override public String toString() {
            return "?" + name + "@" + subpattern;
        }
    }

    /**
     * A pattern that requires the target value to have a specific type
     * name.
     * @param typeName The required type name.
     */
    record TypeName(String typeName) implements Pattern {
        /**
         * Returns "name" as the string representation.
         * @return The string
         */
        @Override public String toString() {
            return typeName + "()";
        }
    }

    /**
     * A pattern that binds the matching target value to the binding variable
     * with the given name.
     * @param name A binding variable name
     */
    record ValueBinding(String name) implements Pattern {
        /**
         * Returns "?name" as the string representation.
         * @return The string
         */
        @Override public String toString() {
            return "?" + name;
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
}
