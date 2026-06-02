package com.wjduquette.joe.tools;

import java.util.Deque;

/**
 * A function for parsing the given option, and its value (if any) from
 * the queue of arguments.  The option has already been removed from the
 * queue.  There are three possible outcomes:
 *
 * <ol>
 * <li>The parser doesn't recognize the option, and returns false.</li>
 * <li>The parser recognizes the option, parses it successfully, and
 * returns true, stashing any value somewhere known to the object whose
 * method the parser is.</li>
 * <li>The parser recognizes the option but detects the error and halts
 * the program with an appropriate error message.</li>
 * </ol>
 */
public interface OptionParser {
    /**
     * Parses the given option if known, setting any appropriate variables.
     * See the class comment for the required semantics.
     *
     * @param option The option
     * @param argq   The queue of arguments.
     * @return true or false
     */
    boolean parse(String option, Deque<String> argq);
}
