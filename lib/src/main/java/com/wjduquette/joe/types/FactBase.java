package com.wjduquette.joe.types;

import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.FactSet;

import java.util.Collection;

/**
 * A Nero
 * {@link com.wjduquette.joe.nero.FactSet} augmented for use as a Joe
 * FactBase.
 */
public class FactBase extends FactSet {
    //-------------------------------------------------------------------------
    // Instance Variables

    private boolean debug = false;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an empty FactBase.
     */
    public FactBase() {
        super();
    }

    /**
     * Creates a FactBase containing the given facts.
     * @param facts The facts
     */
    public FactBase(Collection<Fact> facts) {
        super(facts);
    }

    /**
     * Creates a FactBase containing the given facts from the FactSet
     * @param other The fact set
     */
    public FactBase(FactSet other) {
        super(other);
    }

    //-------------------------------------------------------------------------
    // Accessors

    /**
     * Whether the Nero debug flag is set or not.
     * @return true or false
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Sets the Nero debug flag.
     * @param debug true or false
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
