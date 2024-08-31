package com.wjduquette.joe.tools;

import java.util.function.Consumer;

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
