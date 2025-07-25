package com.wjduquette.joe.tools;

import java.util.Deque;

/**
 * Base type for tools.  Provides default methods for standard helpers.
 */
@SuppressWarnings("unused")
public interface Tool {
    /**
     * Returns the tool's metadata.
     * @return The metadata
     */
    ToolInfo toolInfo();

    //-------------------------------------------------------------------------
    // Defaulted Tool API

    /**
     * Prints a newline to System.out.
     */
    default void println() {
        System.out.println();
    }

    /**
     * Prints an object to System.out.
     * @param object The object
     */
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

    /**
     * Given an option, looks for the option's value in the argument
     * queue.
     * @param opt The option
     * @param argq The queue
     * @return The value
     * @throws ToolException if the option has no value.
     */
    default String toOptArg(String opt, Deque<String> argq) {
        if (!argq.isEmpty()) {
            return argq.poll();
        } else if (opt.startsWith("-")) {
            throw error("missing value for option " + opt);
        } else {
            throw expected("option",  opt);
        }
    }

    /**
     * Converts the argument to a value of the enumeration.
     * @param cls The enum class
     * @param arg The argument
     * @return The enum constant.
     * @param <E> The enum type.
     */
    default <E extends Enum<E>> E toEnum(Class<E> cls, String arg) {
        try {
            return Enum.valueOf(cls, arg.toUpperCase());
        } catch (Exception ex) {
            throw expected(cls.getSimpleName(), arg);
        }
    }

    /**
     * Converts the option's value to a value of the enumeration.
     * @param cls The enum class
     * @param opt The option
     * @param argq The argument queue
     * @return The enum constant.
     * @param <E> The enum type.
     */
    default <E extends Enum<E>> E toEnum(
        Class<E> cls,
        String opt,
        Deque<String> argq
    ) {
        return toEnum(cls, toOptArg(opt, argq));
    }

    /**
     * Returns a ToolException for an "unknown option" error.
     * @param opt The option.
     * @return The exception
     */
    default ToolException unknownOption(String opt) {
        return new ToolException("Unknown option: \"" + opt + "\".");
    }

    /**
     * Returns a ToolException for an "expected this, got that" error.
     * @param expected what was expected
     * @param got what was received.
     * @return The exception
     */
    default ToolException expected(String expected, String got) {
        return new ToolException(
            "Expected " + expected + ", got: \"" + got + "\".");
    }

    /**
     * Simply returns a ToolException with the given message.
     * @param message The message
     * @return The exception
     */
    default ToolException error(String message) {
        return new ToolException(message);
    }

    /**
     * Returns a ToolException with the given message and cause.
     * @param message The message
     * @param cause The cause
     * @return The exception
     */
    default ToolException error(String message, Throwable cause) {
        return new ToolException(message, cause);
    }

    /**
     * Exits the tool with a code of 0.
     */
    default void exit() {
        exit(0);
    }

    /**
     * Exits the tool with the given code.
     * @param code The code
     */
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
