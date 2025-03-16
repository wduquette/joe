package com.wjduquette.joe.types;

import com.wjduquette.joe.*;

import java.util.List;

/**
 * A Joe-equivalent for the Java StringBuilder.
 */
public class TextBuilder implements JoeValue {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The object infrastructure
    private final JoeValueCore core;

    // The buffer
    private StringBuilder buff = new StringBuilder();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a TextBuilder.
     * @param joeClass The Joe class for which this is the Java instance.
     */
    public TextBuilder(JoeClass joeClass) {
        this.core = new JoeValueCore(joeClass, this);
    }

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

    //-------------------------------------------------------------------------
    // JoeValue API

    @Override public JoeType type() { return core.type(); }
    @Override public String typeName() { return core.typeName(); }
    @Override public List<String> getFieldNames() { return core.getFieldNames(); }
    @Override public boolean hasField(String name) { return core.hasField(name); }
    @Override public Object get(String name) { return core.get(name); }
    @Override public void set(String name, Object value) { core.set(name, value); }
    @Override public String stringify(Joe joe) { return core.stringify(joe); }
}
