package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A JavaFX ListView for displaying arbitrary Joe values.  Supports
 * extension by Joe classes, value-to-string conversion using
 * Joe::stringify or the client's own stringifier, and safe selection without
 * logic loops.
 */
public class ListViewInstance extends ListView<Object> implements JoeValue {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The interpreter, needed by the list cell
    private final Joe joe;

    // The core, which handles JavaFX properties, etc.
    private final JoeValueCore core;

    // Whether a programmatic select*() call is in progress.
    private boolean inSelect = false;

    // The client's stringifier
    private Function<Object,String> stringifier = null;

    // The client's `onSelect` handler
    private Consumer<ListViewInstance> onSelect = null;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an instance of ListViewInstance
     * @param joe The Joe interpreter
     * @param joeClass The class info, e.g., from `ListViewClass`.
     */
    public ListViewInstance(Joe joe, JoeClass joeClass) {
        this.joe = joe;
        this.core = new JoeValueCore(joeClass, this);

        // Use MyCell instead of the standard ListCell.
        setCellFactory(p -> new MyCell());

        // Call the user's onSelect handler when the **user** selects
        // an item in the list.
        getSelectionModel().selectedItemProperty()
            .addListener((p,o,n) -> onSelectItem());
    }

    //-------------------------------------------------------------------------
    // JoeValueCore delegations

    @Override public JoeType type() { return core.type(); }
    @Override public List<String> getFieldNames() { return core.getFieldNames(); }
    @Override public Object get(String name) { return core.get(name); }
    @Override public void set(String name, Object value) { core.set(name, value); }
    @Override public String stringify(Joe joe) { return core.stringify(joe); }

    //-------------------------------------------------------------------------
    // ListViewInstance Logic

    // Called when an item is selected, by the user or programmatically.
    // Calls the user's onSelect handler only if the item was selected
    // by the user.
    private void onSelectItem() {
        if (!inSelect && onSelect != null) {
            onSelect.accept(this);
        }
    }

    // Stringifies a value for display by MyCell.
    private String stringifyValue(Object value) {
        return stringifier != null
            ? stringifier.apply(value)
            : joe.stringify(value);
    }

    //-------------------------------------------------------------------------
    // ListViewInstance public API

    /**
     * Gets the onSelect handler, which takes the ListViewInstance as an argument.
     * @return The handler
     */
    @SuppressWarnings("unused")
    public Consumer<ListViewInstance> getOnSelect() {
        return onSelect;
    }
    /**
     * Sets the onSelect handler, which takes the ListViewInstance as an argument.
     * @param handler The handler
     */
    public void setOnSelect(Consumer<ListViewInstance> handler) {
        this.onSelect = handler;
    }

    /**
     * Gets the widget's stringifier, or null if none is set.
     * @return The stringifier
     */
    @SuppressWarnings("unused")
    public Function<Object, String> getStringifier() {
        return stringifier;
    }

    /**
     * The widget's selected index property, derived from its selection model.
     * @return The property.
     */
    @SuppressWarnings("unused")
    public ReadOnlyIntegerProperty selectedIndexProperty() {
        return getSelectionModel().selectedIndexProperty();
    }

    /**
     * Gets the widget's stringifier, the function used to convert values to
     * strings for display (in place of Joe::stringify), or null if none is set.
     * @param function The stringifier
     */
    public void setStringifier(Function<Object, String> function) {
        this.stringifier = function;
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

    //-------------------------------------------------------------------------
    // MyCell

    // A ListCell that converts an object to a string using
    // either Joe::stringify or the client's stringifier.
    private class MyCell extends ListCell<Object> {
        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText("");
            } else {
                setText(stringifyValue(item));
            }
        }
    }
}
