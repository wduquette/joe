package com.wjduquette.joe.nero;

import com.wjduquette.joe.JoeError;

import java.util.*;

/**
 * A Schema for the facts inferred by a Nero program.  Nero can infer the
 * shapes for inferred facts from the program's axioms and rule heads, or
 * shapes can be pre-defined.  Either way, Nero will ensure that all
 * facts produced by the program for a particular relation have the
 * same shape.
 */
public class Schema {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The relation shapes, by relation name.
    private final Map<String,Shape> shapeMap = new HashMap<>();

    // The set of transient relations.  Transient relations are dropped after
    // inference is complete.
    private final Set<String> transients = new HashSet<>();

    //-------------------------------------------------------------------------
    // Constructor

    public Schema() {
        // Nothing to do.
    }

    //-------------------------------------------------------------------------
    // Public API

    /**
     * Returns true if the relation is marked transient in this schema.
     * @param relation The relation name
     * @return true or false
     */
    public boolean isTransient(String relation) {
        return transients.contains(relation);
    }

    /**
     * Sets/clears the transient flag for the given relation.
     * @param relation The relation name
     * @param flag The transient flag
     */
    public void setTransient(String relation, boolean flag) {
        if (flag) {
            transients.add(relation);
        } else {
            transients.remove(relation);
        }
    }

    public Set<String> getTransients() {
        return transients;
    }

    /**
     * Gets whether or not a shape is defined for this relation.
     * @param relation The relation
     * @return true or false
     */
    public boolean hasRelation(String relation) {
        return shapeMap.containsKey(relation);
    }

    /**
     * Gets a set of the names of the relations for which a shape is known.
     * @return The set
     */
    public Set<String> getRelations() {
        return shapeMap.keySet();
    }

    /**
     * Returns the shape for the given relation, or null if none.
     * @param relation The relation
     * @return The shape, or null
     */
    public Shape get(String relation) {
        return shapeMap.get(relation);
    }

    /**
     * Adds the shape to the schema if no shape is defined for the
     * shape's relation.  Otherwise, verifies that the given shape is
     * identical to the defined shape.  Returns false if there is a mismatch
     * and true otherwise.
     * @param shape The shape to add
     * @return true or false
     */
    public boolean checkAndAdd(Shape shape) {
        if (shapeMap.containsKey(shape.relation())) {
            return get(shape.relation()).equals(shape);
        } else {
            shapeMap.put(shape.relation(), shape);
            return true;
        }
    }

    /**
     * Adds the shape inferred from the fact to the schema if no shape is
     * defined for the fact's relation.  Otherwise, verifies that
     * the inferred shape this is identical to the defined shape.
     * Returns false if there is a mismatch and true otherwise.
     * @param fact The fact whose shape is to be added.
     * @return true or false
     */
    public boolean checkAndAdd(Fact fact) {
        var shape = Shape.inferShape(fact);
        return checkAndAdd(shape);
    }

    /**
     * Adds the shape inferred from the head atom to the schema if no shape is
     * defined for the atom's relation.  Otherwise, verifies that the defined
     * shape is compatible with the atom. Returns false if there is a mismatch
     * and true otherwise.
     *
     * <p>If a shape is defined, compatibility is determined as follows:</p>
     * <ul>
     * <li>For a ListShape, the atom must be an ordered atom of the correct
     *     arity.</li>
     * <li>For a MapShape, the atom must be a named atom.</li>
     * <li>For a PairShape, the atom must be an ordered atom of the correct
     *     arity.</li>
     * </ul>
     * @param head The head atom whose shape is to be added.
     * @return true or false
     */
    public boolean checkAndAdd(Atom head) {
        var relation = head.relation();
        var defined = get(relation);

        // FIRST, save the inferred shape if there's no shape already defined.
        if (defined == null) {
            shapeMap.put(relation, Shape.inferDefaultShape(head));
            return true;
        }

        // NEXT, make sure the atom is compatible with the defined shape.
        return Shape.conformsTo(head, defined);
    }

    //-------------------------------------------------------------------------
    // Static API

    /**
     * Infers a schema from a collection of facts.  Throws an error
     * if facts with the same relation have different shapes.
     * @param facts The facts
     * @return the schema
     * @throws JoeError on shape mismatch
     */
    public static Schema inferSchema(Collection<? extends Fact> facts) {
        var schema = new Schema();

        for (var fact : facts) {
            if (!schema.checkAndAdd(fact)) {
                throw new JoeError("Shape mismatch for fact: '" + fact + "'.");
            }
        }

        return schema;
    }
}
