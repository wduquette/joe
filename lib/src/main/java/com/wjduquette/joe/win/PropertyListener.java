package com.wjduquette.joe.win;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.Keyword;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * An object that listens to a JavaFX property and can be canceled.
 */
public class PropertyListener
    implements Listener, ChangeListener<Object>
{
    private final Property<?> property;
    private final Joe joe;
    private final Keyword keyword;
    private final Object callable;

    public PropertyListener(
        Property<?> property,
        Joe joe,
        Keyword keyword,
        Object callable
    ) {
        this.property = property;
        this.joe = joe;
        this.keyword = keyword;
        this.callable = callable;
    }

    @Override
    public void changed(
        ObservableValue<?> ignored,
        Object oldValue,
        Object newValue
    ) {
        joe.call(callable, keyword, oldValue, newValue);
    }

    @Override
    public String toString() {
        return "PropertyListener[" + keyword + "]@" +
            String.format("%x", hashCode());
    }

    @Override
    public void cancel() {
        property.removeListener(this);
    }
}
