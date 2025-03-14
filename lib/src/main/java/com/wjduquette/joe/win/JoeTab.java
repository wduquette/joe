package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import javafx.scene.control.Tab;

/**
 * A JavaFX Tab that can be extended by Joe classes.
 */
public class JoeTab extends Tab implements JoeValue {
    private final JoeValueCore core;

    /**
     * Creates a JoeTab.
     * @param joeClass The Joe class for which this is the Java instance.
     */
    public JoeTab(JoeClass joeClass) {
        this.core = new JoeValueCore(joeClass, this);
    }

    @Override public JoeType type() { return core.type(); }
    @Override public String typeName() { return core.typeName(); }
    @Override public boolean hasField(String name) { return core.hasField(name); }
    @Override public Object get(String name) { return core.get(name); }
    @Override public void set(String name, Object value) { core.set(name, value); }
    @Override public String stringify(Joe joe) { return core.stringify(joe); }
}
