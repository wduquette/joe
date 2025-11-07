package com.wjduquette.joe.app;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.PackageFinder;
import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.pkg.text.JoeTextPackage;
import com.wjduquette.joe.console.ConsolePackage;
import com.wjduquette.joe.tools.Tool;
import com.wjduquette.joe.tools.ToolInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.List;

/**
 * The implementation for the {@code joe repl} tool.
 */
public class ReplTool implements Tool {
    /**
     * Tool information for this tool, for use by the launcher.
     */
    public static final ToolInfo INFO =ToolInfo.define()
        .name("repl")
        .argsig("[options...]")
        .oneLiner("Invokes a simple Joe REPL.")
        .help("""
        Invokes the REPL.
        
        - To exit, press ^D.
        - To execute a statement, enter it as it would appear in a
          script.
        - To evaluate an expression, terminate it with a semicolon:
        
            > 1 + 1;
            -> 2
            >
        
        The tool searches for locally installed Joe packages on a user-provided
        library path, which must be a colon-delimited list of folder paths.
        By default, the library path is provided by the JOE_LIB_PATH environment
        variable (if defined); otherwise, the user can specify a library path
        via the --libpath option.
        
        The options are as follows:
        
        The options are as follows:
        
        --libpath path, --l path
           Sets the library path to the given path.
        --clark, -c
            Use the "Clark" byte-engine (default)
        --walker, -w
            Use the "Walker" AST-walker engine.
        --debug, -d
            Enable debugging output.  This is mostly of use to
            the Joe maintainer.
        """)
        .launcher(ReplTool::main)
        .build();

    //-------------------------------------------------------------------------
    // Instance Variables

    private Joe joe;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the tool
     */
    public ReplTool() {
    }

    //-------------------------------------------------------------------------
    // Execution

    /**
     * Gets the tool's implementation info.
     * @return The info
     */
    public ToolInfo toolInfo() {
        return INFO;
    }

    private void run(String[] args) {
        var argq = new ArrayDeque<>(List.of(args));
        var engineType = Joe.CLARK;
        String libPath = null;
        var debug = false;

        while (!argq.isEmpty() && argq.peek().startsWith("-")) {
            var opt = argq.poll();
            switch (opt) {
                case "--libpath", "-l" -> libPath = toOptArg(opt, argq);
                case "--walker", "-w" -> engineType = Joe.WALKER;
                case "--clark",  "-c" -> engineType = Joe.CLARK;
                case "--debug",  "-d" -> debug = true;
                default -> {
                    System.err.println("Unknown option: '" + opt + "'.");
                    System.exit(64);
                }
            }
        }

        this.joe = new Joe(engineType);
        joe.setDebug(debug);

        var consolePackage = new ConsolePackage();
        consolePackage.setScript("<repl>");
        consolePackage.getArgs().addAll(List.of(args));
        joe.registerPackage(JoeTextPackage.PACKAGE);
        joe.installPackage(consolePackage);
        var found = PackageFinder.find(libPath != null
            ? libPath
            : System.getenv(Joe.JOE_LIB_PATH));
        joe.registerPackages(found);

        try {
            System.out.println("Joe " + App.getVersion() + " (" +
                joe.engineName() + " engine)");
            runPrompt();
        } catch (IOException ex) {
            System.err.print(
                "I/O Error while reading from console: " +
                ex.getMessage());
        }
    }

    void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            try {
                var result = joe.run("%repl%", line);

                if (result != null) {
                    System.out.println("-> " + joe.stringify(result));
                }
            } catch (SyntaxError ex) {
                System.out.println(ex.getErrorReport());
                System.out.println("*** " + ex.getMessage());
            } catch (JoeError ex) {
                System.out.println("*** " + ex.getJoeStackTrace());
            }
        }
    }



    //-------------------------------------------------------------------------
    // Main

    /**
     * The tool's main routine.
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        new ReplTool().run(args);
    }
}
