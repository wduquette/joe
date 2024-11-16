package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import javafx.scene.control.ListView;

import java.util.function.Consumer;

public class JoeListView extends ListView<Object> implements JoeObject {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The interpreter, needed by the list cell
    private final Joe joe;

    // The core, which handles JavaFX properties, etc.
    private final JoeObjectCore core;

    // Whether a programmatic select*() call is in progress.
    private boolean inSelect = false;

    // The client's `onSelect` handler
    private Consumer<JoeListView> onSelect = null;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an instance of JoeListView
     * @param joe The Joe interpreter
     * @param joeClass The class info, e.g., from `ListViewProxy`.
     */
    public JoeListView(Joe joe, JoeClass joeClass) {
        this.joe = joe;
        this.core = new JoeObjectCore(joeClass, this);

        // Call the user's onSelect handler when the **user** selects
        // an item in the list.
        getSelectionModel().selectedItemProperty()
            .addListener((p,o,n) -> onSelectItem());
    }

    //-------------------------------------------------------------------------
    // JoeObjectCore delegations

    @Override public String typeName() { return core.typeName(); }
    @Override public Object get(String name) { return core.get(name); }
    @Override public void set(String name, Object value) { core.set(name, value); }

    //-------------------------------------------------------------------------
    // JoeListView Logic

    // Called when an item is selected, by the user or programmatically.
    // Calls the user's onSelect handler only if the item was selected
    // by the user.
    private void onSelectItem() {
        if (!inSelect && onSelect != null) {
            onSelect.accept(this);
        }
    }

    //-------------------------------------------------------------------------
    // JoeListView public API

    /**
     * Gets the onSelect handler, which takes the JoeListView as an argument.
     * @return The handler
     */
    @SuppressWarnings("unused")
    public Consumer<JoeListView> getOnSelect() {
        return onSelect;
    }
    /**
     * Sets the onSelect handler, which takes the JoeListView as an argument.
     * @param handler The handler
     */
    public void setOnSelect(Consumer<JoeListView> handler) {
        this.onSelect = handler;
    }

    /**
     * Select the item at the given index.
     * @param index The index
     * @throws JoeError if the index is out of range is out of range.
     * @throws JoeError if called from the `onSelect` handler.
     */
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

    /**
     * Selects the given item.
     * TODO: What happens if the item is not found?
     * @param item The item
     * @throws JoeError if the index is out of range is out of range.
     * @throws JoeError if called from the `onSelect` handler.
     */
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
}
