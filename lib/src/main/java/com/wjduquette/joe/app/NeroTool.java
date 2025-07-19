package com.wjduquette.joe.app;

import com.wjduquette.joe.*;
import com.wjduquette.joe.nero.Nero;
import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.tools.ToolInfo;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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
        "Executes Nero scripts.",
        """
        Nero is Joe's implementation of Datalog.  This tool executes Nero
        scripts and supports Nero debugging.
        
        By default, the tool executes the rule set and outputs all computed
        facts.
        
        Option:
        
        --ast, -a                 Dumps the abstract syntax tree (AST)
        --debug, -d               Enable debugging
        --all                     Dumps all known facts.
        --relation, -r relation   Dumps facts with the given relation.
        """,
        NeroTool::main
    );

    //-------------------------------------------------------------------------
    // Instance Variables

    private final Nero nero = new Nero(new Joe());

    private boolean dumpAST = false;
    private boolean dumpAll = false;
    private final List<String> relations = new ArrayList<>();

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
        // FIRST, parse the arguments
        var argq = new ArrayDeque<>(List.of(args));

        while (!argq.isEmpty() && argq.peek().startsWith("-")) {
            var opt = argq.poll();
            switch (opt) {
                case "--ast", "-a"      -> dumpAST = true;
                case "--all"            -> dumpAll = true;
                case "--debug", "-d"    -> nero.setDebug(true);
                case "--relation", "-r" ->
                    relations.add(toOptArg(opt, argq));
                default -> {
                    System.err.println("Unknown option: '" + opt + "'.");
                    System.exit(64);
                }
            }
        }

        if (argq.isEmpty()) {
            printUsage(App.NAME);
            System.exit(64);
        }

        // NEXT, get the source.
        var path = argq.poll();
        SourceBuffer source = null;

        try {
            source = readSource(path);
        } catch (IOException ex) {
            System.err.println("Could not read script: " + path +
                "\n*** " + ex.getMessage());
            System.exit(1);
        }
        assert source != null;

        // NEXT, figure out what to do.
        var dumpNew = !dumpAST && !dumpAll && relations.isEmpty();

        try {
            // FIRST, dump the AST if they asked for that.
            if (dumpAST) println(nero.dumpAST(source));

            // NEXT, compile and execute it.
            var results = nero.execute(source);

            if (dumpNew) {
                var newFacts = new HashSet<>(results.getInferredFacts());
                newFacts.removeAll(results.getAxioms());
                dumpFacts("New Facts:", newFacts);
            } else if (dumpAll) {
                // If we dump everything, no need to do specific queries.
                dumpFacts("All Facts:", results.getKnownFacts().getAll());
            } else {
                for (var relation : relations) {
                    dumpFacts("Relation: " + relation,
                        results.getKnownFacts().getRelation(relation));
                }
            }
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

    private void dumpFacts(String title, Collection<Fact> facts) {
        println(title);
        facts.stream().map(this::factString).sorted()
            .forEach(this::println);
    }

    private String factString(Fact fact) {
        var fields = fact.getFields().stream()
            .map(this::fieldString)
            .collect(Collectors.joining(", "));
        return fact.relation() + "(" + fields + ")";
    }

    private String fieldString(Object field) {
        if (field instanceof String s) {
            return Joe.quote(s);
        } else {
            return Objects.toString(field);
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
