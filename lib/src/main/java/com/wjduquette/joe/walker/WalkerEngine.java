package com.wjduquette.joe.walker;

import com.wjduquette.joe.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class WalkerEngine implements Engine {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The owning instance of Joe
    private final Joe joe;

    // The interpreter
    private final Interpreter interpreter;

    // The buffers
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<String,SourceBuffer> buffers = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    public WalkerEngine(Joe joe) {
        this.joe = joe;
        interpreter = new Interpreter(joe);
    }

    //-------------------------------------------------------------------------
    // Engine API


    @Override
    public Set<String> getVarNames() {
        return interpreter.globals().getVarNames();
    }

    @Override
    public Object getVar(String name) {
        return interpreter.globals().getVar(name);
    }

    @Override
    public void setVar(String name, Object value) {
        interpreter.globals().setVar(name, value);
    }

    /**
     * Reads the given file and executes its content as a script.
     * @param scriptPath The file's path
     * @return The script's result
     * @throws IOException if the file cannot be read.
     * @throws SyntaxError if the script could not be compiled.
     * @throws JoeError on all runtime errors.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Object runFile(String scriptPath)
        throws IOException, SyntaxError, JoeError
    {
        var path = Paths.get(scriptPath);
        byte[] bytes = Files.readAllBytes(path);
        var script = new String(bytes, Charset.defaultCharset());

        return run(path.getFileName().toString(), script);
    }

    /**
     * Executes the script, throwing an appropriate error on failure.
     * @param filename The source of the input.
     * @param source The input
     * @return The script's result
     * @throws SyntaxError if the script could not be compiled.
     * @throws JoeError on all runtime errors.
     */
    public Object run(String filename, String source) throws SyntaxError, JoeError {
        var details = new ArrayList<SyntaxError.Detail>();

        Scanner scanner = new Scanner(filename, source, details::add);
        List<Token> tokens = scanner.scanTokens();
        var buffer = scanner.buffer();
        Parser parser = new Parser(buffer, tokens, details::add);
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

        // Save the buffer, for later introspection.
        buffers.put(filename, buffer);

        return interpreter.interpret(statements);
    }

    /**
     * Calls a JoeCallable value with the given arguments.
     * @param callee A Joe value which must be callable.
     * @param args The arguments to pass to the callable
     * @return The result of calling the callable.
     */
    public Object call(Object callee, Object... args) {
        if (callee instanceof JoeCallable callable) {
            return callable.call(joe, new Args(args));
        } else {
            throw joe.expected("callable", callee);
        }
    }
}
