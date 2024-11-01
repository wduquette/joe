package com.wjduquette.joe;

import java.io.IOException;

/**
 * An Engine is a parsing and execution engine for the Joe language.
 * It provides a clean interface between the parsing and execution
 * code and the rest of the Joe ecosystem: the embedding and extension
 * API, the standard library, etc.  As such it allows the creation of
 * experimental execution engines that work within the same ecosystem.
 */
public interface Engine {
    /**
     * Gets the global environment.
     * @return The global environment.
     */
    GlobalEnvironment globals();

    /**
     * Reads the given file and executes its content as a script.
     * @param path The file's path
     * @return The script's result
     * @throws IOException if the file cannot be read.
     * @throws SyntaxError if the script could not be compiled.
     * @throws JoeError on all runtime errors.
     */
    Object runFile(String path)
        throws IOException, SyntaxError, JoeError;

    /**
     * Executes the script, throwing an appropriate error on failure.
     * @param source The input
     * @return The script's result
     * @throws SyntaxError if the script could not be compiled.
     * @throws JoeError on all runtime errors.
     */
    Object run(String source) throws SyntaxError, JoeError;
}
