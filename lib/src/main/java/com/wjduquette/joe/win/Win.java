package com.wjduquette.joe.win;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import javafx.scene.layout.Priority;

/**
 * This static class defines a variety of services for widget types.
 */
public class Win {
    private Win() {} // Static class

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
