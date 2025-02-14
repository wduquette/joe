package com.wjduquette.joe.tools;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * This is the base class for JavaFX-based tools.  The tool class should:
 *
 * <ul>
 * <li>Subclass FXTool.</li>
 * <li>Implement the {@code run(stage, argq)} method.</li>
 * <li>Implement the usual JavaFX {@code main()}, as show below. </li>
 * <li>Define a static {@code ToolInfo} record that gives {@code main} as the
 *     tool's launcher.</li>
 * </ul>
 *
 * <pre>
 *     public static void main(String[] args) {
 *         launch(args);
 *     }
 * </pre>
 */
public abstract class FXTool extends Application implements Tool {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final ToolInfo toolInfo;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the tool object given the info.
     * @param toolInfo The info
     */
    public FXTool(ToolInfo toolInfo) {
        this.toolInfo = toolInfo;
    }

    //-------------------------------------------------------------------------
    // FXTool Implementation

    /**
     * This is the usual JavaFX application start method.  It will acquire
     * the raw command line arguments, build the argument queue, and execute
     * the {@code run} method, taking care to handle all exceptions thrown
     * by {@code run} or by subsequent events.
     * @param stage the primary stage
     */
    @Override
    public void start(Stage stage) {
        // FIRST, create the argument queue.
        var argq = new ArrayDeque<>(getParameters().getRaw());

        // NEXT, prepare to handle uncaught exceptions in the background.
        Thread.currentThread().setUncaughtExceptionHandler(
            (thread, ex) -> handleUncaughtException(false, ex));

        // NEXT, execute the run method.
        try {
            run(stage, argq);
        } catch (Throwable ex) {
            handleUncaughtException(true, ex);
        }
    }

    //-------------------------------------------------------------------------
    // Tool API

    @Override
    public ToolInfo toolInfo() {
        return toolInfo;
    }

    //-------------------------------------------------------------------------
    // FXTool API

    /**
     * Subclasses must implement this method.  All exceptions thrown will be
     * caught and handled.
     * @param stage The primary stage
     * @param argq The argument queue
     * @throws Exception on execution error.
     */
    public abstract void run(Stage stage, Deque<String> argq) throws Exception;
}
