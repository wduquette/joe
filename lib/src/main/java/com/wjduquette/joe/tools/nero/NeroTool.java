package com.wjduquette.joe.tools.nero;

import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.app.App;
import com.wjduquette.joe.nero.Engine;
import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.tools.ToolInfo;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.List;

/**
 * The implementation for the {@code joe nero} tool.
 */
public class NeroTool implements Tool {
    /**
     * Tool information for this tool, for use by the launcher.
     */
    public static final ToolInfo INFO = new ToolInfo(
        "nero",
        "[options...] file.nero",
        "Experimental tool for working with Nero scripts.",
        """
        Exercises the Nero script in some way.
        """,
        NeroTool::main
    );

    //-------------------------------------------------------------------------
    // Constructor

    /** Creates the tool. */
    public NeroTool() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // Execution

    /**
     * Gets implementation info about the tool.
     * @return The info.
     */
    public ToolInfo toolInfo() {
        return INFO;
    }

    private void run(String[] args) {
        var argq = new ArrayDeque<>(List.of(args));

        if (argq.isEmpty()) {
            printUsage(App.NAME);
            System.exit(64);
        }

        var path = argq.poll();


        try {
            executeFile(path);
        } catch (IOException ex) {
            System.err.println("Could not read script: " + path +
                "\n*** " + ex.getMessage());
            System.exit(1);
        } catch (SyntaxError ex) {
            System.err.println(ex.getErrorReport());
            System.err.println("*** " + ex.getMessage());
            System.exit(65);
        } catch (JoeError ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }

    /**
     * Processes the given file in some way.
     * @param scriptPath The file's path
     * @throws IOException if the file cannot be read.
     * @throws JoeError if the script could not be compiled.
     */
    public void executeFile(String scriptPath)
        throws IOException, SyntaxError
    {
        var path = Paths.get(scriptPath);
        byte[] bytes = Files.readAllBytes(path);
        var script = new String(bytes, Charset.defaultCharset());

        execute(new SourceBuffer(path.getFileName().toString(), script));
    }

    /**
     * Just a convenient entry point for getting some source code into
     * the module.  This will undoubtedly change a lot over time.
     *
     * @param buff The Nero source.
     * @throws JoeError if the script could not be compiled.
     */
    public void execute(SourceBuffer buff) {
        // FIRST, compile the source.
        var compiler = new Compiler(buff);
        var ruleset = compiler.compile();

        // Will throw JoeError if the rules aren't stratified.
        var engine = new Engine(ruleset);

        try {
            engine.infer();
            System.out.println("\nKnown facts:");
            engine.getKnownFacts().stream().map(Fact::toString).sorted()
                .forEach(f -> System.out.println("  " + f));

        } catch (Exception ex) {
            System.out.println("Error in ruleset: " + ex);
        }
    }


    //-------------------------------------------------------------------------
    // Main

    /**
     * The tool's main routine.
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        new NeroTool().run(args);
    }
}
