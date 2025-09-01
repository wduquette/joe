package com.wjduquette.joe;

import java.util.Set;

/**
 * An Engine is a parsing and execution engine for the Joe language.
 * It provides a clean interface between the parsing and execution
 * code and the rest of the Joe ecosystem: the embedding and extension
 * API, the standard library, etc.  As such it allows the creation of
 * experimental execution engines that work within the same ecosystem.
 */
public interface Engine {
    /**
     * Gets the engine's global environment.
     * @return The environment
     */
    Environment getEnvironment();

    /**
     * Executes the script, throwing an appropriate error on failure.
     * The filename is usually the bare file name of the script file,
     * but can be any string relevant to the application, e.g., "%repl%".
     * @param filename The filename
     * @param source The input
     * @return The script's result
     * @throws SyntaxError if the script could not be compiled.
     * @throws JoeError on all runtime errors.
     */
    Object run(String filename, String source) throws SyntaxError, JoeError;

    /**
     * Compiles the script and returns a compilation dump,
     * throwing an appropriate error on failure.
     * The filename is usually the bare file name of the script file,
     * but can be any string relevant to the application, e.g., "%repl%".
     * @param filename The filename
     * @param source The input
     * @return The dump
     * @throws SyntaxError if the script could not be compiled.
     */
    String dump(String filename, String source) throws SyntaxError;

    /**
     * Calls a JoeCallable value with the given arguments.
     * @param callee A Joe value which must be callable.
     * @param args The arguments to pass to the callable
     * @return The result of calling the callable.
     */
    Object call(Object callee, Object... args);

    /**
     * Returns true if the callee is callable in this engine,
     * and false otherwise.
     * @param callee The callee
     * @return true or false
     */
    boolean isCallable(Object callee);
}
