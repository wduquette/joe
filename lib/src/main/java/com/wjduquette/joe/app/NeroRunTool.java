package com.wjduquette.joe.app;

import com.wjduquette.joe.*;
import com.wjduquette.joe.nero.FactSet;
import com.wjduquette.joe.nero.Nero;
import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.tools.ToolInfo;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * The implementation for the {@code joe nero} tool.
 */
public class NeroRunTool implements Tool {
    /**
     * Tool information for this tool, for use by the launcher.
     */
    public static final ToolInfo INFO = new ToolInfo(
        "run",
        "[options...] file.nero [file.nero...]",
        "Executes Nero scripts.",
        """
        Nero is Joe's implementation of Datalog.  This tool executes Nero
        scripts and supports Nero debugging.
        
        The tool executes the provided rule sets in sequence, accumulating the
        inferred facts, which are then written output as a Nero script.
        
        Option:
        
        --ast, -a
            Dumps the abstract syntax tree (AST) to standard output.
        
        --debug, -d
            Enable execution debugging
        
        --out filename, -o filename
            Writes the inferred facts to the given file.
        """,
        NeroRunTool::main
    );

    //-------------------------------------------------------------------------
    // Instance Variables

    private boolean debug = false;
    private boolean dumpAST = false;
    private String outFile = null;

    //-------------------------------------------------------------------------
    // Constructor

    /** Creates the tool. */
    public NeroRunTool() {
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
        // FIRST, parse the arguments
        var argq = new ArrayDeque<>(List.of(args));

        while (!argq.isEmpty() && argq.peek().startsWith("-")) {
            var opt = argq.poll();
            switch (opt) {
                case "--ast", "-a"    -> dumpAST = true;
                case "--debug", "-d"  -> debug = false;
                case "--out", "-o"    -> outFile = toOptArg(opt, argq);
                default -> {
                    System.err.println("Unknown option: '" + opt + "'.");
                    System.exit(1);
                }
            }
        }

        if (argq.isEmpty()) {
            printUsage(App.NAME);
            System.exit(1);
        }

        var inputs = new ArrayList<>(argq);

        try {
            if (dumpAST) {
                dumpASTs(inputs);
            } else {
                execute(inputs);
            }
        } catch (IOException ex) {
            System.err.println("Error reading input: " + ex.getMessage());
            System.exit(1);
        } catch (SyntaxError ex) {
            System.err.println(ex.getErrorReport());
            System.err.println("*** " + ex.getMessage());
            System.exit(1);
        } catch (JoeError ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }

    private void dumpASTs(List<String> inputs) throws IOException {
        for (var name : inputs) {
            var source = readSource(name);
            println("AST: " + name);
            println(Nero.parse(source).toString());
        }
    }

    private void execute(List<String> inputs) throws IOException {
        var db = new FactSet();

        for (var name : inputs) {
            var source = readSource(name);
            Nero.with(source).debug(debug).update(db);
        }

        if (outFile == null) {
            println(Nero.toNeroScript(db));
        } else {
            println("Writing: " + outFile);
            var path = Path.of(outFile);
            if (Files.exists(path)) {
                Files.copy(path, Path.of(outFile + "~"));
            }
            Files.writeString(path, Nero.toNeroScript(db));
        }
    }

    /**
     * Gets the source from disk.
     * @param scriptPath The file's path
     * @throws IOException if the file cannot be read.
     */
    private SourceBuffer readSource(String scriptPath)
        throws IOException
    {
        var path = Paths.get(scriptPath);
        byte[] bytes = Files.readAllBytes(path);
        var script = new String(bytes, Charset.defaultCharset());

        return new SourceBuffer(path.getFileName().toString(), script);
    }

    //-------------------------------------------------------------------------
    // Main

    /**
     * The tool's main routine.
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        new NeroRunTool().run(args);
    }
}
