package com.wjduquette.joe.win;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeClass;
import com.wjduquette.joe.JoeObject;
import com.wjduquette.joe.JoeObjectCore;
import javafx.scene.layout.HBox;

/**
 * A JavaFX HBox that can be extended by Joe classes.
 */
public class JoeHBox extends HBox implements JoeObject {
    private final JoeObjectCore core;

    /**
     * Creates a JoeHBox.
     * @param joeClass The Joe class for which this is the Java instance.
     */
    public JoeHBox(JoeClass joeClass) {
        this.core = new JoeObjectCore(joeClass, this);
    }

    @Override public String typeName() { return core.typeName(); }
    @Override public boolean hasField(String name) { return core.hasField(name); }
    @Override public Object get(String name) { return core.get(name); }
    @Override public void set(String name, Object value) { core.set(name, value); }
    @Override public String stringify(Joe joe) { return core.stringify(joe); }
}
