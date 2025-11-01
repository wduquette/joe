package com.wjduquette.joe.tools;

import java.util.function.Consumer;

/**
 * Tool metadata, for used by the ToolLauncher
 * @param name The tool's name (as it appears on the command line)
 * @param argsig its argument signature
 * @param oneLiner A one-line description
 * @param help Full help text
 * @param isJavaFX Is JavaFX required by the tool?
 * @param launcher The launch function
 */
public record ToolInfo(
    String name,
    String argsig,
    String oneLiner,
    String help,
    boolean isJavaFX,
    Consumer<String[]> launcher
) {
    /**
     * Returns a ToolInfo builder.
     * @return The builder
     */
    public static Builder define() {
        return new Builder();
    }

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

    /**
     * The ToolInfo builder.
     */
    public static class Builder {
        //---------------------------------------------------------------------
        // Instance variables

        String name = null;
        String argsig = "";
        String oneLiner = "";
        String help = "";
        boolean isJavaFX = false;
        Consumer<String[]> launcher = null;

        //---------------------------------------------------------------------
        // Constructor

        /**
         * Creates the builder.
         */
        public Builder() {
            // Nothing to do
        }

        //---------------------------------------------------------------------
        // API

        /**
         * Sets the tool's name.
         * @param name The name
         * @return The builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the tool's argument signature.
         * @param sig The signature
         * @return The builder
         */
        public Builder argsig(String sig) {
            this.argsig = sig;
            return this;
        }

        /**
         * Sets the tool's one-line description
         * @param oneLiner The description
         * @return The builder
         */
        public Builder oneLiner(String oneLiner) {
            this.oneLiner = oneLiner;
            return this;
        }

        /**
         * Sets the tool's help text.
         * @param help The text
         * @return The builder
         */
        public Builder help(String help) {
            this.help = help;
            return this;
        }

        /**
         * Sets whether this is a non-GUI tool or a JavaFX tool.
         * @param flag true or false
         * @return The builder
         */
        public Builder javafx(boolean flag) {
            this.isJavaFX = flag;
            return this;
        }

        /**
         * Sets the launcher function.
         * @param launcher The launcher
         * @return The builder
         */
        public Builder launcher(Consumer<String[]> launcher ) {
            this.launcher = launcher;
            return this;
        }

        /**
         * Builds the value given the input.
         * @return The ToolInfo value
         */
        public ToolInfo build() {
            return new ToolInfo(
                name,
                argsig,
                oneLiner,
                help,
                isJavaFX,
                launcher
            );
        }
    }
}
