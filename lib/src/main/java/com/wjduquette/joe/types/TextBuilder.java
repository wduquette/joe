package com.wjduquette.joe.types;

import com.wjduquette.joe.*;

import java.util.HashMap;
import java.util.Map;

/**
 * A Joe replacement for the Java StringBuilder.
 */
public class TextBuilder implements JoeInstance {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The Joe class and field map.
    private final JoeClass joeClass;
    private final Map<String,Object> fieldMap = new HashMap<>();

    // The buffer
    private StringBuilder buff = new StringBuilder();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a TextBuilder.
     * @param joeClass The Joe class for which this is the Java instance.
     */
    public TextBuilder(JoeClass joeClass) {
        this.joeClass = joeClass;
    }

    //-------------------------------------------------------------------------
    // NativeInstance API

    @Override public JoeClass getJoeClass() { return joeClass; }
    @Override public Map<String, Object> getInstanceFieldMap() { return fieldMap; }

    //-------------------------------------------------------------------------
    // TextBuilder API

    /**
     * Clears the buffer.
     */
    public void clear() {
        this.buff = new StringBuilder();
    }

    /**
     * Appends a string.
     * @param value The string.
     * @return this
     */
    public TextBuilder append(String value) {
        buff.append(value);
        return this;
    }

    /**
     * Gets the accumulated string.
     * @return The string
     */
    public String toString() {
        return buff.toString();
    }
}
