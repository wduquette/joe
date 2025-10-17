package com.wjduquette.joe.win;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.wrappers.CallbackWrapper;
import javafx.event.Event;
import javafx.event.EventHandler;

/**
 * A {@link CallbackWrapper} for JavaFX events.  It will invoke a Joe
 * callable with one argument, the event.
 * @param <E> The event type
 */
class JoeNoArgEventHandler<E extends Event>
    extends CallbackWrapper implements EventHandler<E>
{
    /**
     * Creates the wrapper
     * @param joe The Joe interpreter
     * @param callable The callable
     */
    public JoeNoArgEventHandler(Joe joe, Object callable) {
        super(joe, callable);
    }

    @Override
    public void handle(E event) {
        callCallable(); // Ignore the event.
    }
}
