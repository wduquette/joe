package com.wjduquette.joe.win;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeClass;
import com.wjduquette.joe.JoeObject;
import com.wjduquette.joe.JoeObjectCore;
import javafx.scene.layout.HBox;

public class JoeHBox extends HBox implements JoeObject {
    private final JoeObjectCore core;

    public JoeHBox(JoeClass joeClass) {
        this.core = new JoeObjectCore(joeClass, this);
    }

    @Override public String typeName() { return core.typeName(); }
    @Override public Object get(String name) { return core.get(name); }
    @Override public void set(String name, Object value) { core.set(name, value); }
    @Override public String stringify(Joe joe) { return core.stringify(joe); }
}
