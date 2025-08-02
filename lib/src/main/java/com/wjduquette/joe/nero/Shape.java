package com.wjduquette.joe.nero;

import java.util.List;

public sealed interface Shape permits
    Shape.ListShape,
    Shape.MapShape,
    Shape.PairShape
{
    //-------------------------------------------------------------------------
    // Interface API

    /**
     * The shape's relation name.
     * @return The name
     */
    String relation();

    /**
     * The arity of facts having this shape.  Returns -1 if the arity can
     * vary for different facts of this shape.
     * @return The arity
     */
    int arity();

    /**
     * Gets the string representation of the shape, as it appears
     * in `define` declarations.
     * @return The string representation.
     */
    String toSpec();

    //-------------------------------------------------------------------------
    // Concrete Shape Types

    /**
     * The shape of a ListFact having the given relation and arity.
     * @param relation The relation name
     * @param arity The arity
     */
    record ListShape(String relation, int arity)
        implements Shape
    {
        @Override public String toSpec() { return relation + "/" + arity; }
    }

    /**
     * The shape of a MapFact having the given relation.
     * @param relation
     */
    record MapShape(String relation)
        implements Shape
    {
        @Override public int arity() { return -1; }
        @Override public String toSpec() { return relation + "/..."; }
    }

    /**
     * The shape of a PairFact having the given relation and field names.
     * @param relation The relation
     * @param fieldNames The field names.
     */
    record PairShape(String relation, List<String> fieldNames)
        implements Shape
    {
        @Override public int arity() { return fieldNames.size(); }
        @Override public String toSpec() {
            return relation + "/" + String.join(",", fieldNames);
        }
    }

    //-------------------------------------------------------------------------
    // Static API

    /**
     * Infer the shape of a fact from the fact.  List, map, and pair facts have
     * the list, map, and pair shapes.
     * @param fact The fact
     * @return The shape
     */
    static Shape inferShape(Fact fact) {
        return switch (fact) {
            case ListFact f -> new ListShape(f.relation(), f.arity());
            case MapFact f  -> new MapShape(f.relation());
            case PairFact f -> new PairShape(f.relation(), f.getFieldNames());
        };
    }

    /**
     * Infer the default shape of a fact from a rule or axiom head atom.  Infers the
     * list shape for ordered atoms and the map shape for named atoms.
     * @param head The head atom
     * @return The inferred shape
     */
    static Shape inferDefaultShape(Atom head) {
        return switch (head) {
            case OrderedAtom a -> new ListShape(a.relation(), a.terms().size());
            case NamedAtom a -> new MapShape(a.relation());
        };
    }

    /**
     * Returns true if the atom conforms to the given Shape, and false
     * otherwise.
     * @param atom The atom
     * @param shape The shape
     * @return true or false
     */
    static boolean conformsTo(Atom atom, Shape shape) {
        return switch (shape) {
            case Shape.ListShape s ->
                atom instanceof OrderedAtom a &&
                    s.arity() == a.terms().size();
            case Shape.MapShape ignored ->
                atom instanceof NamedAtom;
            case Shape.PairShape s ->
                atom instanceof OrderedAtom a &&
                    s.arity() == a.terms().size();
        };
    }
}
