package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import javafx.scene.control.Tab;

import java.util.HashMap;
import java.util.Map;

/**
 * A JavaFX Tab that can be extended by Joe classes.
 */
public class TabInstance extends Tab implements NativeInstance {
    // The Joe class and field map
    private final JoeClass joeClass;
    private final Map<String,Object> fieldMap = new HashMap<>();

    /**
     * Creates a TabInstance.
     * @param joeClass The Joe class for which this is the Java instance.
     */
    public TabInstance(JoeClass joeClass) {
        this.joeClass = joeClass;
    }

    @Override public JoeClass getJoeClass() { return joeClass; }
    @Override public Map<String, Object> getInstanceFieldMap() { return fieldMap; }
}
