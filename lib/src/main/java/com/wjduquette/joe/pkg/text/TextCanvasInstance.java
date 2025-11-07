package com.wjduquette.joe.pkg.text;

import com.wjduquette.joe.JoeClass;
import com.wjduquette.joe.JoeInstance;
import com.wjduquette.joe.types.text.TextCanvas;

import java.util.HashMap;
import java.util.Map;

/**
 * Extensible TextCanvas.
 */
public class TextCanvasInstance
    extends TextCanvas
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
     * Creates a TextCanvasInstance.
     * @param joeClass The Joe class for which this is the Java instance.
     */
    public TextCanvasInstance(JoeClass joeClass) {
        this.joeClass = joeClass;
    }

    //-------------------------------------------------------------------------
    // JoeInstance API

    @Override public JoeClass getJoeClass() { return joeClass; }
    @Override public Map<String, Object> getInstanceFieldMap() { return fieldMap; }
}
