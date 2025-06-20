package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import javafx.scene.layout.StackPane;

import java.util.List;

/**
 * A JavaFX StackPane that can be extended by Joe classes.
 */
public class StackPaneInstance extends StackPane implements JoeValue {
    private final JoeValueCore core;

    /**
     * Creates a StackPaneInstance.
     * @param joeClass The Joe class for which this is the Java instance.
     */
    public StackPaneInstance(JoeClass joeClass) {
        this.core = new JoeValueCore(joeClass, this);
    }

    @Override public JoeType type() { return core.type(); }
    @Override public List<String> getFieldNames() { return core.getFieldNames(); }
    @Override public Object get(String name) { return core.get(name); }
    @Override public void set(String name, Object value) { core.set(name, value); }
    @Override public String stringify(Joe joe) { return core.stringify(joe); }
}
