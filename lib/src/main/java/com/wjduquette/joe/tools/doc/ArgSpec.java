package com.wjduquette.joe.tools.doc;

import com.wjduquette.joe.Joe;

import java.util.Arrays;
import java.util.List;

/**
 * An ArgSpec is a function or method argument spec.  Nominally, it's a
 * comma-delimited list of identifiers.  Use square brackets to indicate
 * optional arguments/groups of arguments, and "..." to indicate an argument
 * list of indefinite length.
 */
public class ArgSpec {
    private ArgSpec() {} // Static class

    /**
     * Validates an arg spec
     * @return The spec
     */
    public static boolean isValid(String spec) {
        for (var name : names(spec)) {
            if (!Joe.isIdentifier(name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the names of the arguments.
     * @return The list of names
     */
    public static List<String> names(String spec) {
        // FIRST, if there's nothing.
        if (spec.isBlank()) {
            return List.of();
        }

        // NEXT, split out the words
        var names = spec.split("[ ,.\\[\\]]+");
        return Arrays.asList(names);
    }

    /**
     * Returns the arg spec as a Markdown string.
     * @return The list of names
     */
    public static String asMarkdown(String spec) {
        var tokens = spec.splitWithDelimiters("[ ,.\\[\\]]+", 0);

        StringBuilder buff = new StringBuilder();
        for (var token : tokens) {
            if (Joe.isIdentifier(token)) {
                // Italicize
                buff.append("*").append(token).append("*");
            } else {
                buff.append(token.replace("[", "\\["));
            }
        }

        return buff.toString();
    }
}
