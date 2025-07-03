package com.wjduquette.joe.types;

import com.wjduquette.joe.nero.FactSet;

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

    public FactBase() {
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
