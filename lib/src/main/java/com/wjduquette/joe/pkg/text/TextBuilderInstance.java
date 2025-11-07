package com.wjduquette.joe.pkg.text;

import com.wjduquette.joe.JoeClass;
import com.wjduquette.joe.JoeInstance;
import com.wjduquette.joe.types.text.TextBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Extensible TextBuilder.
 */
public class TextBuilderInstance
    extends TextBuilder
    implements JoeInstance
{
    //-------------------------------------------------------------------------
    // Instance Variables

    // The Joe class and field map.
    private final JoeClass joeClass;
    private final Map<String,Object> fieldMap = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a TextBuilderInstance.
     * @param joeClass The Joe class for which this is the Java instance.
     */
    public TextBuilderInstance(JoeClass joeClass) {
        this.joeClass = joeClass;
    }

    //-------------------------------------------------------------------------
    // JoeInstance API

    @Override public JoeClass getJoeClass() { return joeClass; }
    @Override public Map<String, Object> getInstanceFieldMap() { return fieldMap; }
}
