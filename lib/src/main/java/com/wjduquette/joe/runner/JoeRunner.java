package com.wjduquette.joe.runner;

import com.wjduquette.joe.*;
import com.wjduquette.joe.console.ConsolePackage;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * JoeRunner is a framework for writing non-GUI script execution tools like
 * {@code joe run}.  It does not presume the client is
 * using the {@link com.wjduquette.joe.tools.Tool} framework.
 */
public class JoeRunner {
    //------------------------------------------------------------------------
    // Static API

    /**
     * Begins the process of defining the runner.
     * @return the builder.
     */
    public static Builder define() {
        return new Builder();
    }

    //------------------------------------------------------------------------
    // Instance Variables

    // Configuration
    private final String appName;
    private final String engineType;
    private final boolean debug;
    private final String libPath;
    private final String scriptPath;
    private final List<String> scriptArgs;
    private final List<JoePackage> installed;
    private final List<JoePackage> registered;
    private final Consumer<Joe> onConfigure;

    // Execution
    private Joe joe;
    private Duration runTime = null;

    //------------------------------------------------------------------------
    // Constructor

    private JoeRunner(Builder builder) {
        this.appName = builder.appName;
        this.engineType = builder.engineType;
        this.debug = builder.debug;
        this.libPath = builder.libPath;
        this.scriptPath = builder.scriptPath;
        this.scriptArgs = builder.scriptArgs;
        this.installed = builder.installed;
        this.registered = builder.registered;
        this.onConfigure = builder.onConfigure;

        if (scriptPath == null) {
            throw new IllegalStateException("No script was provided.");
        }
    }

    //------------------------------------------------------------------------
    // Public API

    /**
     * Executes the configured script.
     */
    public void execute() {
        // FIRST, create the interpreter
        joe = new Joe(engineType);
        joe.setDebug(debug);

        // NEXT, install and register the default packages.
        var consolePackage = new ConsolePackage();
        consolePackage.setScript(scriptPath);
        consolePackage.getArgs().addAll(scriptArgs);
        joe.installPackage(consolePackage);

        // NEXT, install and register packages requested by the client.
        installed.forEach(joe::installPackage);
        registered.forEach(joe::registerPackage);

        // NEXT, run the client's onConfigure handler.
        if (onConfigure != null) {
            onConfigure.accept(joe);
        }

        // NEXT, find and register packages from known repositories.
        if (libPath != null) {
            joe.registerPackages(PackageFinder.find(libPath));
        }

        try {
            if (debug) {
                System.out.println(appName + " (Joe " +
                    joe.engineName() + " engine)");
            }
            var startTime = Instant.now();
            joe.runFile(scriptPath);
            var endTime = Instant.now();
            runTime = Duration.between(startTime, endTime);
        } catch (IOException ex) {
            System.err.println("Could not read script: " + scriptPath +
                "\n*** " + ex.getMessage());
            System.exit(1);
        } catch (SyntaxError ex) {
            System.err.println(ex.getErrorReport());
            System.err.println("*** " + ex.getMessage());
            System.exit(65);
        } catch (JoeError ex) {
            System.err.print("*** Error in script: ");
            System.err.println(ex.getJoeStackTrace());
            System.exit(70);
        }
    }

    /**
     * After a successful run, get the Joe interpreter.
     * @return The interpreter
     */
    public Joe getJoe() {
        return joe;
    }

    /**
     * After a successful run, this method returns the duration of script
     * execution.
     * @return The runtime.
     */
    public Duration getRunTime() {
        return runTime;
    }

    //------------------------------------------------------------------------
    // Builder

    /**
     * A builder for defining a Joe script runner.
     */
    public static class Builder {
        //--------------------------------------------------------------------
        // Instance Variables

        private String appName = "Joe";
        private String engineType = Joe.CLARK;
        private boolean debug = false;
        private String libPath = null;
        private String scriptPath = null;
        private List<String> scriptArgs = null;
        private final List<JoePackage> installed = new ArrayList<>();
        private final List<JoePackage> registered = new ArrayList<>();
        private Consumer<Joe> onConfigure = null;

        //--------------------------------------------------------------------
        // Constructor

        /**
         * Creates a new builder.
         */
        public Builder() {
            // Nothing to do
        }

        //--------------------------------------------------------------------
        // Methods

        /**
         * Builds the runner given the options.
         * @return The runner
         */
        public JoeRunner build() {
            return new JoeRunner(this);
        }

        /**
         * Sets the runner's application name
         * @return The runner
         */
        public Builder appName(String value) {
            this.appName = value;
            return this;
        }

        /**
         * Sets the runner's Joe engine type, which defaults to the
         * default engine type.
         * @return The runner
         */
        public Builder engineType(String value) {
            this.engineType = value;
            return this;
        }

        /**
         * Sets the Joe debug flag, which defaults to false.
         * @param flag true or false
         * @return this
         */
        public Builder debug(boolean flag) {
            this.debug = flag;
            return this;
        }

        /**
         * Sets the library path, a colon-delimited list of folders that
         * contain Joe package repositories.  Defaults to null.
         * @param value The path
         * @return this
         */
        public Builder libPath(String value) {
            this.libPath = value;
            return this;
        }

        /**
         * Sets the path and file name of a script to be executed.  Required.
         * @param value The path
         * @return this
         */
        public Builder scriptPath(String value) {
            this.scriptPath = value;
            return this;
        }

        /**
         * Sets the arguments to pass to the script.
         * @param value The collection
         * @return this
         */
        public Builder scriptArgs(Collection<String> value) {
            this.scriptArgs = new ArrayList<>(value);
            return this;
        }

        /**
         * Explicitly registers and installs the given package.
         * @param pkg The package
         * @return this
         */
        @SuppressWarnings("unused")
        public Builder install(JoePackage pkg) {
            this.installed.add(pkg);
            return this;
        }

        /**
         * Explicitly registers but does not install the given package.
         * @param pkg The package
         * @return this
         */
        @SuppressWarnings("unused")
        public Builder register(JoePackage pkg) {
            this.registered.add(pkg);
            return this;
        }

        /**
         * Specifies a handler to call just before the script is executed.
         * This allows the client to do further customization of the
         * interpreter.
         * @param handler The handler
         * @return this
         */
        @SuppressWarnings("unused")
        public Builder onConfigure(Consumer<Joe> handler) {
            this.onConfigure = handler;
            return this;
        }
    }
}
