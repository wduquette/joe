package com.wjduquette.joe.binder;

import java.util.List;
import java.util.Map;

/**
 * A binding Pattern is used to destructure a complex data structure and
 * bind values within it to Joe variables.  As such, Pattern is an
 * algebraic data type (ADT) used to compose complex patterns in ways
 * that match data structures.
 *
 * <p>Several of the Pattern variants have a variable ID field, which
 * identifies a binding variable to receive the matched value.  The
 * variable ID is an arbitrary Java object, as the {@link Binder} is
 * independent of the language engine, and the type of the ID will differ
 * from engine to engine, and possible from differ for different kinds of
 * variable.</p>
 */
public sealed interface Pattern permits
    Pattern.ListPattern, Pattern.MapPattern,
    Pattern.SubPatternBinding,
    Pattern.ValueBinding, Pattern.ValuePattern,
    Pattern.Wildcard
{
    /**
     * A pattern that matches a target
     * {@link com.wjduquette.joe.JoeList}.  The patterns in the
     * {@code patterns} list must match the corresponding items in the
     * target list.  The overall match fails if they do not, or if there are
     * too few items in the target list.
     *
     * <p>If the {@code tailId} is null then the match will also fail if
     * there are excess items in the target list.  Otherwise,
     * {@code tailId} will identify a binding variable, and a list containing
     * the excess items will be bound to that variable.
     * @param patterns The item patterns
     * @param tailId The ID for the tail variable, or null.
     */
    record ListPattern(List<Pattern> patterns, Object tailId) implements Pattern {}

    /**
     * A pattern that matches a target
     * {@link com.wjduquette.joe.JoeMap}.  Each key constant in the
     * {@code patterns} map must exist as a key in the target map, and
     * each key's value pattern in the {@code patterns} map must match
     * the key's value in the target map.
     * @param patterns The keys and value patterns
     */
    record MapPattern(Map<Object,Pattern> patterns) implements Pattern {}

    /**
     * A pattern that matches a sub-pattern and binds its value to a binding
     * variable.
     * @param varId A binding variable Id
     * @param pattern The sub-pattern to match.
     */
    record SubPatternBinding(Object varId, Pattern pattern) implements Pattern {}

    /**
     * A pattern that binds the matching target value to the binding variable
     * with the given ID.
     * @param varId A binding variable ID
     */
    record ValueBinding(Object varId) implements Pattern {}

    /**
     * A pattern that requires the target value to be exactly equal to a
     * constant value.
     * @param value The constant value
     */
    record ValuePattern(Object value) implements Pattern {}

    /**
     * A pattern that matches any value at all.  A wildcard's name is an
     * identifier with an initial underscore.  A single underscore,
     * "{@code _}", is common, but longer names can be used to improve
     * readability.  The name has no effect at all on the pattern
     * match.
     * @param name The wildcard name.
     */
    record Wildcard(String name) implements Pattern {}
}
