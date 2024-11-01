package com.wjduquette.joe;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WalkerEngine implements Engine {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Interpreter interpreter;

    //-------------------------------------------------------------------------
    // Constructor

    public WalkerEngine(Joe joe) {
        interpreter = new Interpreter(joe);
    }

    //-------------------------------------------------------------------------
    // Engine API

    public GlobalEnvironment globals() {
        return interpreter.globals();
    }

    /**
     * Reads the given file and executes its content as a script.
     * @param path The file's path
     * @return The script's result
     * @throws IOException if the file cannot be read.
     * @throws SyntaxError if the script could not be compiled.
     * @throws JoeError on all runtime errors.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Object runFile(String path)
        throws IOException, SyntaxError, JoeError
    {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        var script = new String(bytes, Charset.defaultCharset());

        return run(script);
    }

    /**
     * Executes the script, throwing an appropriate error on failure.
     * @param source The input
     * @return The script's result
     * @throws SyntaxError if the script could not be compiled.
     * @throws JoeError on all runtime errors.
     */
    public Object run(String source) throws SyntaxError, JoeError {
        var details = new ArrayList<SyntaxError.Detail>();

        Scanner scanner = new Scanner(source, details::add);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens, details::add);
        var statements = parser.parse();

        // Stop if there was a syntax error.
        if (!details.isEmpty()) {
            throw new SyntaxError("Syntax error in input, halting.", details);
        }

        Resolver resolver = new Resolver(interpreter, details::add);
        resolver.resolve(statements);

        // Stop if there was a resolution error.
        if (!details.isEmpty()) {
            throw new SyntaxError("Syntax error in input, halting.", details);
        }

        return interpreter.interpret(statements);
    }
}
