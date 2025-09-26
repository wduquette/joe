package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import javafx.scene.layout.HBox;

import java.util.HashMap;
import java.util.Map;

/**
 * A JavaFX HBox that can be extended by Joe classes.
 */
public class HBoxInstance extends HBox implements JoeInstance {
    // The Joe class and field map
    private final JoeClass joeClass;
    private final Map<String,Object> fieldMap = new HashMap<>();

    /**
     * Creates a HBoxInstance.
     * @param joeClass The Joe class for which this is the Java instance.
     */
    public HBoxInstance(JoeClass joeClass) {
        this.joeClass = joeClass;
    }

    @Override public JoeClass getJoeClass() { return joeClass; }
    @Override public Map<String, Object> getInstanceFieldMap() { return fieldMap; }
}
