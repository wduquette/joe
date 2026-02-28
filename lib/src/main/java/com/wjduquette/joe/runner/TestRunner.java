package com.wjduquette.joe.runner;

import com.wjduquette.joe.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * TestRunner is a framework for writing non-GUI script execution tools like
 * {@code joe test}.  It does not presume the client is using the
 * {@link com.wjduquette.joe.tools.Tool} framework.
 */
public class TestRunner {
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
    private final Consumer<String> outputHandler;
    private final String appName;
    private final String engineType;
    private final String libPath;
    private final List<JoePackage> installed;
    private final List<JoePackage> registered;
    private final Consumer<Joe> onConfigure;
    private final List<String> testScripts;
    private final boolean verbose;
    private final String testName;

    // Runtime
    private PackageFinder finder = null;
    private int loadErrorCount = 0;
    private int successCount = 0;
    private int skipCount = 0;
    private int failureCount = 0;
    private int errorCount = 0;

    //------------------------------------------------------------------------
    // Constructor

    private TestRunner(Builder builder) {
        this.outputHandler = builder.outputHandler;
        this.appName = builder.appName;
        this.engineType = builder.engineType;
        this.libPath = builder.libPath;
        this.installed = builder.installed;
        this.registered = builder.registered;
        this.onConfigure = builder.onConfigure;
        this.testScripts = builder.testScripts;
        this.verbose = builder.verbose;
        this.testName = builder.testName;

        if (testScripts.isEmpty()) {
            throw new IllegalStateException(
                "No test scripts script were provided.");
        }
    }

    //------------------------------------------------------------------------
    // Public API

    /**
     * Runs the configured tests
     */
    public void run() {
        // FIRST, find any local packages
        if (libPath != null) {
            finder = PackageFinder.find(libPath);
        }

        // NEXT, run the test scripts.  Each test script runs in its own
        // instance of Joe.
        println(appName + " (" + engineType + " engine)");
        var startTime = Instant.now();
        for (var path : testScripts) {
            runTest(path);
        }
        var endTime = Instant.now();
        var runTime = Duration.between(startTime, endTime);
        printf("Run-time: %.3f seconds\n", runTime.toMillis()/1000.0);

        // NEXT, print the final results
        var total = successCount + skipCount + failureCount + errorCount;
        println("");
        printf("Successes  %5d\n", successCount);
        printf("Skipped    %5d\n", skipCount);
        printf("Failures   %5d\n", failureCount);
        printf("Errors     %5d\n", errorCount);
        println("---------- -----");
        printf("Total      %5d\n", total);

        if (loadErrorCount != 0) {
            println("\n*** " + loadErrorCount + " test file(s) failed to load.");
        }

        if (successCount == total && loadErrorCount == 0) {
            println("\nALL TESTS PASS");
        }
    }

    private void runTest(String scriptPath) {
        if (verbose) println("\nRunning: " + scriptPath);

        // NEXT, configure the engine.
        var joe = new Joe(engineType);
        joe.installPackage(new TestPackage(engineType));

        // NEXT, install and register packages requested by the client.
        installed.forEach(joe::installPackage);
        registered.forEach(joe::registerPackage);

        // NEXT, run the client's onConfigure handler.
        if (onConfigure != null) {
            onConfigure.accept(joe);
        }

        // NEXT, register local packages.
        if (finder != null) joe.registerPackages(finder);

        // NEXT, only print script output if the verbose flag is set.
        joe.setOutputHandler(this::testPrinter);

        // NEXT, load the script.
        try {
            joe.runFile(scriptPath);
        } catch (IOException ex) {
            println("Could not read script: " + scriptPath +
                "\n*** " + ex.getMessage());
            ++loadErrorCount;
        } catch (SyntaxError ex) {
            println(ex.getErrorReport());
            println(ex.getMessage());
            ++loadErrorCount;
        } catch (JoeError ex) {
            print("*** Error in script: ");
            println(ex.getJoeStackTrace());
            ++loadErrorCount;
        }

        // NEXT, execute its tests.
        var tests = joe.getVariableNames().stream()
            .filter(name -> name.startsWith("test"))
            .filter(name -> testName == null || name.contains(testName))
            .toList();

        if (tests.isEmpty()) {
            println("***  No tests in: " + scriptPath);
            return;
        }

        if (verbose) {
            println("");
            runVerbosely(joe, tests);
        } else {
            runQuietly(joe, scriptPath, tests);
        }
    }

    private void testPrinter(String message) {
        if (verbose) print(message);
    }

    private void runVerbosely(Joe joe, List<String> tests) {
        for (var test : tests) {
            var callable = joe.getVariable(test);
            if (!joe.isCallable(callable)) {
                continue;
            }

            println("+++ " + test);

            try {
                joe.call(callable);
                ++successCount;
            } catch (TestRunner.SkipError ex) {
                println("  SKIPPED: " + ex.getMessage());
                ++skipCount;
            } catch (AssertError ex) {
                println("  FAILED:\n" + ex.getJoeStackTrace().indent(4));
                ++failureCount;
            } catch (JoeError ex) {
                println("  ERROR:\n" + ex.getJoeStackTrace().indent(4));
                ++errorCount;
            }
        }
    }

    private void runQuietly(Joe joe, String scriptPath, List<String> tests) {
        for (var test : tests) {
            var callable = joe.getVariable(test);
            if (!joe.isCallable(callable)) {
                continue;
            }

            String result;
            Exception error;

            try {
                joe.call(callable);
                ++successCount;
                result = null;
                error = null;
            } catch (TestRunner.SkipError ex) {
                ++skipCount;
                result = "SKIPPED";
                error = ex;
            } catch (AssertError ex) {
                ++failureCount;
                result = "FAILED";
                error = ex;
            } catch (JoeError ex) {
                ++errorCount;
                result = "ERROR";
                error = ex;
            }

            if (error != null) {
                printf("%-8s %s %s\n%s\n", result + ":", scriptPath,
                    test, error.getMessage().indent(4));
            }
        }
    }

    //------------------------------------------------------------------------
    // Output API

    private void print(String text) {
        outputHandler.accept(text);
    }

    private void println(String text) {
        print(text); print("\n");
    }

    private void printf(String fmt, Object... args) {
        print(String.format(fmt, args));
    }

    //-------------------------------------------------------------------------
    // SkipException

    /**
     * An exception used by the test API's "skip()" function.
     */
    public static class SkipError extends JoeError {
        /**
         * Creates the exception.
         * @param message The skip message.
         */
        public SkipError(String message) { super(message); }
    }

    //------------------------------------------------------------------------
    // Builder

    /**
     * A builder for defining a Joe script runner.
     */
    public static class Builder {
        //--------------------------------------------------------------------
        // Instance Variables

        private Consumer<String> outputHandler = System.out::print;
        private String appName = "Joe";
        private String engineType = Joe.CLARK;
        private String libPath = null;
        private final List<JoePackage> installed = new ArrayList<>();
        private final List<JoePackage> registered = new ArrayList<>();
        private Consumer<Joe> onConfigure = null;
        private List<String> testScripts = null;
        private boolean verbose = false;
        private String testName = null;

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
        public TestRunner build() {
            return new TestRunner(this);
        }

        /**
         * Sets the runner's output handler.  Defaults to System.out::print.
         * @return The runner
         */
        @SuppressWarnings("unused")
        public Builder outputHandler(Consumer<String> handler) {
            this.outputHandler = handler;
            return this;
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

        /**
         * Sets the list of test scripts to run.
         * @param value The collection
         * @return this
         */
        public Builder testScripts(Collection<String> value) {
            this.testScripts = new ArrayList<>(value);
            return this;
        }

        /**
         * Sets the verbose flag, which defaults to false.
         * @param flag true or false
         * @return this
         */
        public Builder verbose(boolean flag) {
            this.verbose = flag;
            return this;
        }

        /**
         * Sets a test name for filtering.
         * @param value The name
         * @return this
         */
        public Builder testName(String value) {
            this.testName = value;
            return this;
        }
    }
}
