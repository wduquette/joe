package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import javafx.scene.layout.StackPane;

/**
 * A JavaFX StackPane that can be extended by Joe classes.
 */
public class JoeStackPane extends StackPane implements JoeObject {
    private final JoeObjectCore core;

    /**
     * Creates a JoeStackPane.
     * @param joeClass The Joe class for which this is the Java instance.
     */
    public JoeStackPane(JoeClass joeClass) {
        this.core = new JoeObjectCore(joeClass, this);
    }

    @Override public JoeType type() { return core.type(); }
    @Override public String typeName() { return core.typeName(); }
    @Override public boolean hasField(String name) { return core.hasField(name); }
    @Override public Object get(String name) { return core.get(name); }
    @Override public void set(String name, Object value) { core.set(name, value); }
    @Override public String stringify(Joe joe) { return core.stringify(joe); }
}
