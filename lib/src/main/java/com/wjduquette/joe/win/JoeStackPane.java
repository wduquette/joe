package com.wjduquette.joe.win;

import com.wjduquette.joe.JoeClass;
import com.wjduquette.joe.JoeObject;
import com.wjduquette.joe.JoeObjectCore;
import javafx.scene.layout.StackPane;

public class JoeStackPane extends StackPane implements JoeObject {
    private final JoeObjectCore core;

    public JoeStackPane(JoeClass joeClass) {
        this.core = new JoeObjectCore(joeClass, this);
    }

    @Override public String typeName() { return core.typeName(); }
    @Override public Object get(String name) { return core.get(name); }
    @Override public void set(String name, Object value) { core.set(name, value); }
}