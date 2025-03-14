package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import javafx.scene.layout.GridPane;

/**
 * A JavaFX GridPane that can be extended by Joe classes.
 */
public class JoeGridPane extends GridPane implements JoeObject {
    private final JoeObjectCore core;

    /**
     * Creates a JoeGridPane.
     * @param joeClass The Joe class for which this is the Java instance.
     */
    public JoeGridPane(JoeClass joeClass) {
        this.core = new JoeObjectCore(joeClass, this);
    }

    @Override public JoeType type() { return core.type(); }
    @Override public String typeName() { return core.typeName(); }
    @Override public boolean hasField(String name) { return core.hasField(name); }
    @Override public Object get(String name) { return core.get(name); }
    @Override public void set(String name, Object value) { core.set(name, value); }
    @Override public String stringify(Joe joe) { return core.stringify(joe); }
}
