package com.wjduquette.joe.win;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.Priority;

/**
 * This static class defines a variety of services for widget types.
 */
public class Win {
    private Win() {} // Static class

    /**
     * Converts a callable/1 argument to a wrapped ActionEvent handler.
     * @param joe The interpreter
     * @param arg The callable
     * @return the wrapped handler
     */
    static EventHandler<ActionEvent> toAction(Joe joe, Object arg) {
        return new JoeEventHandler<>(joe, arg);
    }

    /**
     * Converts an argument to a Priority value.
     * @param joe The interpreter
     * @param arg the argument
     * @return The priority
     * @throws JoeError on conversion failure.
     */
    static Priority toPriority(Joe joe, Object arg) {
        return joe.toEnum(arg, Priority.class);
    }

}
