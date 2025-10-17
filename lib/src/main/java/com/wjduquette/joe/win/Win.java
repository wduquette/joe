package com.wjduquette.joe.win;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
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
    public static EventHandler<ActionEvent> toAction(Joe joe, Object arg) {
        return new JoeEventHandler<>(joe, arg);
    }

    /**
     * Converts a callable/0 argument to a wrapped ActionEvent handler.
     * @param joe The interpreter
     * @param arg The callable
     * @return the wrapped handler
     */
    public static EventHandler<ActionEvent> toNoArgAction(Joe joe, Object arg) {
        return new JoeNoArgEventHandler<>(joe, arg);
    }


    /**
     * Converts an argument to a Node value.
     * @param joe The interpreter
     * @param arg the argument
     * @return The node
     * @throws JoeError on conversion failure.
     */
    public static Node toNode(Joe joe, Object arg) {
        return joe.toClass(arg, Node.class);
    }

    /**
     * Converts an argument to a Pos value.
     * @param joe The interpreter
     * @param arg the argument
     * @return The position
     * @throws JoeError on conversion failure.
     */
    public static Pos toPos(Joe joe, Object arg) {
        return joe.toEnum(arg, Pos.class);
    }

    /**
     * Converts an argument to a Priority value.
     * @param joe The interpreter
     * @param arg the argument
     * @return The priority
     * @throws JoeError on conversion failure.
     */
    public static Priority toPriority(Joe joe, Object arg) {
        return joe.toEnum(arg, Priority.class);
    }

    /**
     * Returns the argument as a Tooltip.  If it is already a Tooltip
     * it is returned as is; otherwise, it's converted to a String
     * and used to create a new Tooltip.
     * @param joe the interpreter
     * @param arg the argument
     * @return a tooltip
     */
    public static Tooltip toTooltip(Joe joe, Object arg) {
        return (arg instanceof Tooltip tip)
            ? tip
            : new Tooltip(joe.stringify(arg));
    }
}
