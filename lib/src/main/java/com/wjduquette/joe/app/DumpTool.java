package com.wjduquette.joe.app;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.parser.Parser;
import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.tools.ToolInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation for the {@code joe dump} tool.
 */
public class DumpTool implements Tool {
    /**
     * Tool information for this tool, for use by the launcher.
     */
    public static final ToolInfo INFO = new ToolInfo(
        "dump",
        "[options...] file.joe",
        "Dumps information about the Joe script.",
        """
        Dumps compilation details for the script.  The options
        are as follows:
        
        --code, -c   Dump the compiled byte-code (default)
        --ast,  -a   Dump the Abstract Syntax Tree (AST)
        --bert, -b   Dump the legacy "Bert" byte-code
        """,
        DumpTool::main
    );

    //-------------------------------------------------------------------------
    // Constructor

    /** Creates the tool. */
    public DumpTool() {
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

    private String path = null;
    private String source = null;

    private void run(String[] args) {
        var argq = new ArrayDeque<>(List.of(args));

        if (argq.isEmpty()) {
            printUsage(App.NAME);
            System.exit(64);
        }

        var dumps = new ArrayList<Runnable>();

        while (!argq.isEmpty() && argq.peek().startsWith("-")) {
            var opt = argq.poll();
            switch (opt) {
                case "--code", "-c" -> dumps.add(this::dumpCode);
                case "--ast",  "-a" -> dumps.add(this::dumpAST);
                case "--bert", "-b" -> dumps.add(this::dumpBert);
                default -> {
                    System.err.println("Unknown option: '" + opt + "'.");
                    System.exit(64);
                }
            }
        }

        // Default to --code
        if (dumps.isEmpty()) dumps.add(this::dumpCode);

        path = argq.poll();
        source = readFile(path);

        try {
            var gotDump = false;
            for (var dump : dumps) {
                if (gotDump) println("----------------------------");
                dump.run();
                gotDump = true;
            }
        } catch (SyntaxError ex) {
            System.err.println(ex.getErrorReport());
            System.err.println("*** " + ex.getMessage());
            System.exit(65);
        }
    }

    private void dumpCode() {
        println("Byte-code: " + path + "\n");
        var joe = new Joe(Joe.CLARK);
        println(joe.dump(path, source));
    }

    private void dumpAST() {
        println("AST: " + path + "\n");
        println(Parser.dumpAST(source));
    }

    private void dumpBert() {
        println("Bert byte-code: " + path + "\n");
        var joe = new Joe(Joe.BERT);
        println(joe.dump(path, source));
    }

    private String readFile(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException ex) {
            System.err.println("Could not read script: " + path +
                "\n*** " + ex.getMessage());
            System.exit(1);
            return ""; // make the compiler happy.
        }
    }

    //-------------------------------------------------------------------------
    // Main

    /**
     * The tool's main routine.
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        new DumpTool().run(args);
    }
}
