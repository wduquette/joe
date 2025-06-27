package com.wjduquette.joe.types;

import com.wjduquette.joe.nero.FactSet;

/**
 * A Nero
 * {@link com.wjduquette.joe.nero.FactSet} augmented for use as a Joe
 * FactBase.
 */
public class FactBaseValue extends FactSet {
    //-------------------------------------------------------------------------
    // Instance Variables

    private boolean debug = false;

    //-------------------------------------------------------------------------
    // Constructor

    public FactBaseValue() {
        super();
    }

    //-------------------------------------------------------------------------
    // Accessors

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
