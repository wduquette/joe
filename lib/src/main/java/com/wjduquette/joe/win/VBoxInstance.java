package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

/**
 * A JavaFX VBox that can be extended by Joe classes.
 */
public class VBoxInstance extends VBox implements JoeInstance {
    // The Joe class and field map
    private final JoeClass joeClass;
    private final Map<String,Object> fieldMap = new HashMap<>();

    /**
     * Creates a VBoxInstance.
     * @param joeClass The Joe class for which this is the Java instance.
     */
    public VBoxInstance(JoeClass joeClass) {
        this.joeClass = joeClass;
    }

    @Override public JoeClass getJoeClass() { return joeClass; }
    @Override public Map<String, Object> getInstanceFieldMap() { return fieldMap; }
}
