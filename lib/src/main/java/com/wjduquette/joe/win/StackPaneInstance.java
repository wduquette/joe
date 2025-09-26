package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.Map;

/**
 * A JavaFX StackPane that can be extended by Joe classes.
 */
public class StackPaneInstance extends StackPane implements JoeInstance {
    // The Joe class and field map
    private final JoeClass joeClass;
    private final Map<String,Object> fieldMap = new HashMap<>();

    /**
     * Creates a StackPaneInstance.
     * @param joeClass The Joe class for which this is the Java instance.
     */
    public StackPaneInstance(JoeClass joeClass) {
        this.joeClass = joeClass;
    }

    @Override public JoeClass getJoeClass() { return joeClass; }
    @Override public Map<String, Object> getInstanceFieldMap() { return fieldMap; }
}
