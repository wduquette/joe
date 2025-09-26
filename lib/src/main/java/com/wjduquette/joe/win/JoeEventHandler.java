package com.wjduquette.joe.win;

import com.wjduquette.joe.JoeCallbackWrapper;
import com.wjduquette.joe.Joe;
import javafx.event.Event;
import javafx.event.EventHandler;

/**
 * A {@link JoeCallbackWrapper} for JavaFX events.  It will invoke a Joe
 * callable with one argument, the event.
 * @param <E> The event type
 */
class JoeEventHandler<E extends Event>
    extends JoeCallbackWrapper implements EventHandler<E>
{
    /**
     * Creates the wrapper
     * @param joe The Joe interpreter
     * @param callable The callable
     */
    public JoeEventHandler(Joe joe, Object callable) {
        super(joe, callable);
    }

    @Override
    public void handle(E event) {
        callCallable(event);
    }
}
