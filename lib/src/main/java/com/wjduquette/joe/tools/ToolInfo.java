package com.wjduquette.joe.tools;

import java.util.function.Consumer;

/**
 * Tool metadata, for used by the ToolLauncher
 * @param name The tool's name (as it appears on the command line)
 * @param argsig its argument signature
 * @param oneLiner A one-line description
 * @param help Full help text
 * @param launcher The launch function
 */
public record ToolInfo(
    String name,
    String argsig,
    String oneLiner,
    String help,
    Consumer<String[]> launcher
) {
    /**
     * Prints the tool's usage string to standard output.
     * @param appName The application name
     */
    public void printUsage(String appName) {
        System.out.println("Usage: " + appName + " " + name +
            " " + argsig);
    }

    /**
     * Prints the tool's help text to standard output.
     * @param appName The application name
     */
    public void printHelp(String appName) {
        System.out.println();
        printUsage(appName);
        System.out.println();
        System.out.println(help);
        System.out.println();
    }

}
