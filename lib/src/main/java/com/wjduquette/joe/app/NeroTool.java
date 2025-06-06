package com.wjduquette.joe.app;

import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.Trace;
import com.wjduquette.joe.nero.Nero;
import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.RuleSetCompiler;
import com.wjduquette.joe.parser.ASTRuleSet;
import com.wjduquette.joe.parser.Parser;
import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.tools.ToolInfo;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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

    private boolean dumpAST = false;
    private boolean debug = false;
    private boolean dumpAll = false;
    private final List<String> relations = new ArrayList<>();

    private boolean gotParseError = false;

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
                case "--debug", "-d"    -> debug = true;
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
            if (dumpAST) dumpAST(source);

            // NEXT, compile and execute it.
            var nero = compile(source);

            if (dumpNew) {
                var newFacts = new HashSet<>(nero.getInferredFacts());
                newFacts.removeAll(nero.getAxioms());
                dumpFacts("New Facts:", newFacts);
            } else if (dumpAll) {
                // If we dump everything, no need to do specific queries.
                dumpFacts("All Facts:", nero.getAllFacts());
            } else {
                for (var relation : relations) {
                    dumpFacts("Relation: " + relation,
                        nero.getFacts(relation));
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

    private void dumpAST(SourceBuffer source) {
        var ast = parse(source);
        println(ast);
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

    /**
     * Just a convenient entry point for getting some source code into
     * the module.  This will undoubtedly change a lot over time.
     *
     * @param buff The Nero source.
     * @throws JoeError if the script could not be compiled.
     */
    public Nero compile(SourceBuffer buff) {
        // FIRST, compile the source.
        var ast = parse(buff);
        var compiler = new RuleSetCompiler(ast);
        var ruleset = compiler.compile();

        // Will throw JoeError if the rules aren't stratified.
        var nero = new Nero(ruleset);
        nero.setDebug(debug);
        nero.infer();
        return nero;
    }

    public ASTRuleSet parse(SourceBuffer source) {
        var parser = new Parser(source, this::errorHandler);
        var ast = parser.parseNero();
        if (gotParseError) throw new JoeError("Error in Nero input.");
        return ast;
    }

    private void errorHandler(Trace trace, boolean incomplete) {
        gotParseError = true;
        System.out.println("line " + trace.line() + ": " +
            trace.message());
    }

    void dumpFacts(String title, Collection<Fact> facts) {
        println(title);
        facts.stream().map(Fact::toString).sorted()
            .forEach(this::println);
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
