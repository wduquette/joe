package com.wjduquette.joe.tools.doc;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.app.App;
import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.tools.ToolInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Set;

/**
 * The implementation for the {@code joe doc} tool.
 */
public class DocTool implements Tool {
    /**
     * Tool information for this tool, for use by the launcher.
     */
    public static final ToolInfo INFO = ToolInfo.define()
        .name("doc")
        .argsig("[options...]")
        .oneLiner("Generates documentation from JoeDoc comments.")
        .launcher(DocTool::main)
        .help("""
            The 'joe doc' tool scans a project's Java and Joe source for
            JoeDoc comments and produces API documentation suitable for
            inclusion in an mdBook document.
            
            Options:
            
            --verbose, -v    Enable verbose output.
            """)
        .build();

    /**
     * The set of file types normally scanned for doc comments.
     */
    public static final Set<String> FILE_TYPES = Set.of(
        ".java", ".joe"
    );

    /**
     * The name of the {@code joe doc} tool's configuration file.
     */
    public static final Path DOC_CONFIG = Path.of("doc_config.joe");

    //-------------------------------------------------------------------------
    // Instance Variables

    // The verbose flag
    private boolean verbose = false;

    // The client's configuration
    private final DocConfig config = new DocConfig();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an instance of the DocTool.
     */
    public DocTool() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // Execution

    public ToolInfo toolInfo() {
        return INFO;
    }

    private void run(String[] args) {
        // FIRST, parse the command line.
        var argq = new ArrayDeque<>(List.of(args));

        while (!argq.isEmpty() && argq.peek().startsWith("-")) {
            var opt = argq.poll();
            switch (opt) {
                case "--verbose", "-v" -> verbose = true;
                default -> {
                    System.err.println("Unknown option: '" + opt + "'.");
                    System.exit(64);
                }
            }
        }

        if (!argq.isEmpty()) {
            printUsage(App.NAME);
            System.exit(1);
        }

        // NEXT, look for the doc_config file.
        if (!Files.exists(DOC_CONFIG)) {
            System.err.println("Could not find " + DOC_CONFIG +
                " in the current working directory.");
            exit(1);
        }

        var joe = new Joe();
        joe.installPackage(new JoeDocPackage(config));
        try {
            joe.runFile(DOC_CONFIG.toString());
        } catch (IOException ex) {
            System.err.println("Could not load " + DOC_CONFIG +
                ": " + ex.getMessage());
            exit(1);
        } catch (JoeError ex) {
            System.err.println("Error in " + DOC_CONFIG +
                ": " + ex.getMessage());
            exit(1);
        }

        // NEXT, populate the list of files.
        scanInputFolders();

        if (config.inputFiles().isEmpty()) {
            println("*** No files found.");
            exit();
        }

        // NEXT, parse the files and build up the documentation set.
        var docSet = new DocumentationSet();
        var parser = new DocCommentParser(docSet, verbose);
        var errors = 0;
        for (var file : config.inputFiles()) {
            try {
                parser.parse(file);
            } catch (DocCommentParser.ParseError ex) {
                // The parser has already output the errors.
                ++errors;
            }
        }

        if (errors > 0) {
            println("*** Errors found in JoeDoc input; terminating.");
            exit(1);
        }

//        docSet.dump();

        // NEXT, generate the doc files
        var generator = new Generator(config, docSet, verbose);
        generator.generate();
    }

    private void scanInputFolders() {
        println("Scanning folders:");
        for (var folder : config.inputFolders()) {
            println("  " + folder);
            scanFolder(folder);
        }
    }

    private void scanFolder(Path folder) {
        // Probably have a set of file types; filter on the file's filetype
        // being in the set.  Then clients can add file types easily.

        try (var stream = Files.walk(folder)) {
            stream
                .filter(p -> FILE_TYPES.contains(fileType(p)))
                .forEach(config.inputFiles()::add);
        } catch (IOException ex) {
            println("*** Failed to scan '" + folder + "':\n" +
                ex.getMessage());
        }
    }

    private String fileType(Path path) {
        var text = path.toString();
        var ndx = text.lastIndexOf(".");
        return ndx >= 0 ? text.substring(ndx) : "";
    }


    //-------------------------------------------------------------------------
    // Main

    /**
     * The {@code joe doc} tool's main routine.
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        new DocTool().run(args);
    }
}
