package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A JavaFX ListView for displaying arbitrary Joe values.  Supports
 * extension by Joe classes, value-to-string conversion using
 * Joe::stringify or the client's own formatter, and safe selection without
 * logic loops.
 */
public class ListViewInstance extends ListView<Object> implements JoeInstance {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The Joe class and field map
    private final JoeClass joeClass;
    private final Map<String,Object> fieldMap = new HashMap<>();

    // The interpreter, needed by the list cell
    private final Joe joe;

    // Whether a programmatic select*() call is in progress.
    private boolean inSelect = false;

    // The client's formatter
    private final ObjectProperty<Function<Object,Object>> formatterProperty =
        new SimpleObjectProperty<>();

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
        this.joeClass = joeClass;

        // Use MyCell instead of the standard ListCell.
        setCellFactory(p -> new MyCell());

        // Call the user's onSelect handler when the **user** selects
        // an item in the list.
        getSelectionModel().selectedItemProperty()
            .addListener((p,o,n) -> onSelectItem());
    }

    //-------------------------------------------------------------------------
    // NativeInstance API

    @Override public JoeClass getJoeClass() { return joeClass; }
    @Override public Map<String, Object> getInstanceFieldMap() { return fieldMap; }

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
    private String formatValue(Object value) {
        return formatterProperty.get() != null
            ? Objects.toString(formatterProperty.get().apply(value))
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
     * Gets the widget's formatter, or null if none is set.
     * @return The formatter
     */
    @SuppressWarnings("unused")
    public Function<Object, Object> getFormatter() {
        return formatterProperty.get();
    }

    /**
     * The widget's formatter
     * @return The formatter
     */
    @SuppressWarnings("unused")
    public ObjectProperty<Function<Object, Object>> formatterProperty() {
        return formatterProperty;
    }

    /**
     * Gets the widget's formatter, the function used to convert values to
     * strings for display (in place of Joe::stringify), or null if none is set.
     * @param function The formatter
     */
    public void setFormatter(Function<Object, Object> function) {
        formatterProperty.set(function);
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
    // either Joe::stringify or the client's formatter.
    private class MyCell extends ListCell<Object> {
        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText("");
            } else {
                setText(formatValue(item));
            }
        }
    }
}
