package com.wjduquette.joe.tools.doc;

import com.wjduquette.joe.app.App;
import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.tools.ToolInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DocTool implements Tool {
    /**
     * Tool information for this tool, for use by the launcher.
     */
    public static final ToolInfo INFO = new ToolInfo(
        "doc",
        "",
        "Scans Joe source for doc comments.",
        """
        Work in progress.
        """,
        DocTool::main
    );

    public static final Set<String> FILE_TYPES = Set.of(
        ".java", ".joe"
    );

    //-------------------------------------------------------------------------
    // Instance Variables

    private final List<Path> inputFolders = new ArrayList<>();
    private final List<Path> inputFiles = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

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

        if (!argq.isEmpty()) {
            printUsage(App.NAME);
            System.exit(1);
        }

        // NEXT, get the input folders.
        // NOTE: These will ultimately come from the joe_doc.monica file.
        inputFolders.add(Path.of("../lib/src/main/java/com/wjduquette/joe"));
        inputFolders.add(Path.of("../lib/src/main/resources/com/wjduquette/joe"));

        // NEXT, populate the list of files.
        scanInputFolders();

        if (inputFiles.isEmpty()) {
            println("*** No files found.");
            exit();
        }

        // NEXT, parse the files and build up the documentation set.
        var docSet = new DocumentationSet();
        var parser = new DocCommentParser(docSet);
        inputFiles.forEach(parser::parse);

        docSet.dump();
    }

    private void scanInputFolders() {
        println("Scanning folders:");
        for (var folder : inputFolders) {
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
                .forEach(inputFiles::add);
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

    public static void main(String[] args) {
        new DocTool().run(args);
    }
}
