package com.wjduquette.joe.tools;

import java.util.Deque;

/**
 * Base type for tools.  Provides default methods for standard helpers.
 */
public interface Tool {
    /**
     * Returns the tool's metadata.
     * @return The metadata
     */
    ToolInfo toolInfo();

    //-------------------------------------------------------------------------
    // Defaulted Tool API

    default void println() {
        System.out.println();
    }

    default void println(Object object) {
        System.out.println(object.toString());
    }

    /**
     * Prints the tool's usage string to standard output.
     * @param appName The application name
     */
    default void printUsage(String appName) {
        toolInfo().printUsage(appName);
    }

    default String toOptArg(String opt, Deque<String> argq) {
        if (!argq.isEmpty()) {
            return argq.poll();
        } else if (opt.startsWith("-")) {
            throw error("missing value for option " + opt);
        } else {
            throw expected("option",  opt);
        }
    }

    default <E extends Enum<E>> E toEnum(Class<E> cls, String arg) {
        try {
            return Enum.valueOf(cls, arg.toUpperCase());
        } catch (Exception ex) {
            throw expected(cls.getSimpleName(), arg);
        }
    }

    default <E extends Enum<E>> E toEnum(
        Class<E> cls,
        String opt,
        Deque<String> argq
    ) {
        return toEnum(cls, toOptArg(opt, argq));
    }

    default ToolException unknownOption(String opt) {
        return new ToolException("Unknown option: \"" + opt + "\".");
    }

    default ToolException expected(String expected, String got) {
        return new ToolException(
            "Expected " + expected + ", got: \"" + got + "\".");
    }

    default ToolException error(String message) {
        return new ToolException(message);
    }

    default ToolException error(String message, Throwable cause) {
        return new ToolException(message, cause);
    }

    default void exit() {
        exit(0);
    }

    default void exit(int code) {
        System.exit(code);
    }

    /**
     * Handles uncaught exceptions for the tool.  By default:
     *
     * <ul>
     * <li>{@link ToolException} results in a nice error message.</li>
     * <li>Other throwables result in an "Unexpected exception" message
     *     and a stack trace.</li>
     * <li>Either way, the tool terminates.</li>
     * </ul>
     *
     * <p>Subclasses may override this to provide any desired behavior.</p>
     *
     * @param onRun Whether this occurred in the run method or later
     * @param ex The exception
     */
    @SuppressWarnings("unused")
    default void handleUncaughtException(boolean onRun, Throwable ex) {
        if (ex instanceof ToolException tex) {
            System.err.println(toolInfo().name() + ": " + ex.getMessage());
            if (tex.getCause() != null) {
                System.err.println("   *** " + tex.getCause().getMessage());
            }
            System.exit(1);
        } else {
            System.err.println(toolInfo().name() + ": Unexpected exception,");
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
