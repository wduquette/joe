package com.wjduquette.joe.win;

import com.wjduquette.joe.JoeClass;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.JoeObject;
import com.wjduquette.joe.JoeObjectCore;
import javafx.scene.control.ListView;

import java.util.function.Consumer;

public class JoeListView extends ListView<Object> implements JoeObject {
    //-------------------------------------------------------------------------
    // JoeObject Core

    private final JoeObjectCore core;

    public JoeListView(JoeClass joeClass) {
        this.core = new JoeObjectCore(joeClass, this);
        getSelectionModel().selectedItemProperty()
            .addListener((p,o,n) -> onSelectItem());
    }

    @Override public String typeName() { return core.typeName(); }
    @Override public Object get(String name) { return core.get(name); }
    @Override public void set(String name, Object value) { core.set(name, value); }

    //-------------------------------------------------------------------------
    // ListView enhancements

    private boolean inSelect = false;
    private Consumer<JoeListView> onSelect = null;

    private void onSelectItem() {
        if (!inSelect && onSelect != null) {
            onSelect.accept(this);
        }
    }

    public void selectIndex(int index) {
        if (inSelect) {
            throw new JoeError("Nested selection in ListView.");
        }

        try {
            inSelect = true;
            getSelectionModel().select(index);
        } finally {
            inSelect = false;
        }
    }

    public void selectItem(Object item) {
        if (inSelect) {
            throw new JoeError("Nested selection in ListView.");
        }

        try {
            inSelect = true;
            getSelectionModel().select(item);
        } finally {
            inSelect = false;
        }
    }

    public void setOnSelect(Consumer<JoeListView> handler) {
        this.onSelect = handler;
    }
}
