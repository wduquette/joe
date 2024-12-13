package com.wjduquette.joe;

import java.io.IOException;
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
     * Gets the set of variables from the global environment.
     * @return The names.
     */
    Set<String> getVarNames();

    /**
     * Gets the value of a global variable.
     * @param name The name
     * @return The value
     */
    Object getVar(String name);

    /**
     * Sets the value of a global variable.
     * @param name The name
     * @param value The value
     */
    void setVar(String name, Object value);

    /**
     * Reads the given file and executes its content as a script.
     * @param scriptPath The file's path
     * @return The script's result
     * @throws IOException if the file cannot be read.
     * @throws SyntaxError if the script could not be compiled.
     * @throws JoeError on all runtime errors.
     */
    Object runFile(String scriptPath)
        throws IOException, SyntaxError, JoeError;

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
     * @param scriptPath The file's path
     * @return The dump
     * @throws IOException if the file cannot be read.
     * @throws SyntaxError if the script could not be compiled.
     */
    String dumpFile(String scriptPath) throws IOException, SyntaxError;

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
     * Checks whether the source "is complete", i.e, whether it can
     * be compiled.  The result is not executed.  This is useful
     * in REPLs, so that the user can enter a newline in a string or
     * function call or block and keep editing if it is not complete.
     * @param source The source text
     * @return true or false
     */
    boolean isComplete(String source);

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

    /**
     * Gets whether the engine's debug flag is set.
     * @return true or false
     */
    boolean isDebug();

    /**
     * Sets the engine's debug flag
     * @param flag true or false
     */
    void setDebug(boolean flag);
}
