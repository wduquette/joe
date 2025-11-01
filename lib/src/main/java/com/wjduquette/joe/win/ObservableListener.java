package com.wjduquette.joe.win;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.Keyword;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * An object that listens to a JavaFX ObservableValue and can be canceled.
 */
public class ObservableListener
    implements Listener, ChangeListener<Object>
{
    private final ObservableValue<?> observable;
    private final Joe joe;
    private final Keyword keyword;
    private final Object callable;

    /**
     * Creates an observable listener.
     * @param observable The observable
     * @param joe The Joe interpreter
     * @param keyword The property's keyword
     * @param callable The Joe callable
     */
    public ObservableListener(
        ObservableValue<?> observable,
        Joe joe,
        Keyword keyword,
        Object callable
    ) {
        this.observable = observable;
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
        return "ObservableListener[" + keyword + "]@" +
            String.format("%x", hashCode());
    }

    @Override
    public void cancel() {
        observable.removeListener(this);
    }
}
