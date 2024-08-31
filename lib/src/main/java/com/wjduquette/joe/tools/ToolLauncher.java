/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.wjduquette.joe.tools;

import java.util.*;

/**
 * A launcher for subcommands of a console command.
 */
public class ToolLauncher {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final String appName;
    private final Map<String, ToolInfo> tools = new TreeMap<>();

    private boolean verbose = false;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Initializes the application object.
     */
    public ToolLauncher(String appName, List<ToolInfo> infoList) {
        this.appName = appName;
        infoList.forEach(item -> tools.put(item.name(), item));
    }

    //-------------------------------------------------------------------------
    // Configuration

    @SuppressWarnings("unused")
    public boolean isVerbose() {
        return verbose;
    }

    @SuppressWarnings("unused")
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    //-------------------------------------------------------------------------
    // Launcher

    /**
     * Gets the desired tool and executes it
     * @param args The arguments from the command line.
     */
    public void launch(String[] args) {
        try {
            processArgs(args);
        } catch (ToolException ex) {
            System.err.println("Error: " + ex.getMessage());
            if (verbose) {
                ex.printStackTrace(System.err);
            }
        }
    }

    private void processArgs(String[] args) {
        var argq = new ArrayDeque<>(List.of(args));

        if (argq.isEmpty()) {
            println("Usage: " + appName + " <tool> [<arguments...>]");
            println("");
            println("Run '" + appName + " help' for a list of tools.");
            System.exit(1);
        }

        var subcommand = argq.poll();

        var tool = tools.get(subcommand);

        if (tool != null) {
            tool.launcher().accept(rest(args));
        } else if (subcommand.equals("help")) {
            showHelp(argq);
        } else {
            showFailure(subcommand);
        }
    }

    private String[] rest(String[] args) {
        var rest = new String[args.length - 1];

        System.arraycopy(args, 1, rest, 0, args.length - 1);

        return rest;
    }

    private void showFailure(String subcommand) {
        println("Error, unrecognized tool: '" + subcommand + "'.");
        println("");
        println("Run '" + appName + " help' for a list of subcommands.");
    }

    private void showHelp(Deque<String> argq) {
        if (argq.isEmpty()) {
            println("");
            println("'" + appName +
                "' supports the following tools:\n");

            for (var tool : tools.values()) {
                System.out.printf("%-8s %s\n", tool.name(), tool.oneLiner());
            }

            println("\nEnter '" + appName +
                " help <tool>' for help on a tool.");
            System.out.println();
        } else {
            var subcommand = argq.poll();
            var tool = tools.get(subcommand);

            if (tool != null) {
                tool.printHelp(appName);
            } else {
                showFailure(subcommand);
            }
        }
    }

    //-------------------------------------------------------------------------
    // Helpers

    /**
     * Print to System.out.
     * @param text Text to print.
     */
    public void println(String text) {
        System.out.println(text);
    }
}
