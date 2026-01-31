package com.wjduquette.joe.nero;

import java.util.List;

/**
 * The shape of a Nero relation, for use in a Nero Schema.
 */
@SuppressWarnings("ClassCanBeRecord")
public class Shape {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The relation name
    private final String relation;

    // The field names, or empty if none.
    private final List<String> names;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new shape.  If names is non-empty it will be ordered.
     * @param relation The relation
     * @param names the names.
     */
    public Shape(String relation, List<String> names) {
        this.relation = relation;
        this.names = List.copyOf(names);
    }

    /**
     * Creates a new shape.  If names are provided it will be ordered.
     * @param relation The relation
     * @param names the names, if any.
     */
    public Shape(String relation, String... names) {
        this.relation = relation;
        this.names = List.of(names);
    }

    /**
     * The shape's relation name.
     * @return The name
     */
    public String relation() {
        return relation;
    }

    /**
     * The shape's field names; the list will be empty for unordered relations.
     * @return The list
     */
    public List<String> names() {
        return names;
    }

    /**
     * The arity of facts having this shape.  Returns -1 if the arity can
     * vary for different facts of this shape.
     * @return The arity
     */
    public int arity() {
        return names.size();
    }

    /**
     * Whether facts having this shape are ordered or not.
     * @return true or false
     */
    public boolean isOrdered() {
        return !names.isEmpty();
    }

    /**
     * Whether facts having this shape are unordered or not.
     * @return true or false
     */
    public boolean isUnordered() {
        return names.isEmpty();
    }

    /**
     * Gets the string representation of the shape, as it appears
     * in `define` declarations.
     * @return The string representation.
     */
    public String toSpec() {
        if (isOrdered()) {
            return relation + "/" + String.join(",", names);
        } else {
            return relation + "/...";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Shape shape = (Shape) o;
        return relation.equals(shape.relation) && names.equals(shape.names);
    }

    @Override
    public int hashCode() {
        int result = relation.hashCode();
        result = 31 * result + names.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return toSpec();
    }

    //-------------------------------------------------------------------------
    // Static API

    /**
     * Returns true if the atom conforms to the given Shape, and false
     * otherwise.
     * @param atom The atom
     * @param shape The shape
     * @return true or false
     */
    public static boolean conformsTo(Atom atom, Shape shape) {
        return switch (atom) {
            case MapAtom a -> {
                if (shape.isUnordered()) yield true;
                for (var name : a.termMap().keySet()) {
                    if (!shape.names().contains(name)) yield false;
                }
                yield true;
            }
            case ListAtom a -> shape.arity() == a.terms().size();
        };
    }
}
