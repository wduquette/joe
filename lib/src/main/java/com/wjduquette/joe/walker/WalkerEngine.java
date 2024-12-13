package com.wjduquette.joe.walker;

import com.wjduquette.joe.*;

import java.util.*;
import java.util.stream.Collectors;

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
     * Executes the script, throwing an appropriate error on failure.
     * @param filename The source of the input.
     * @param source The input
     * @return The script's result
     * @throws SyntaxError if the script could not be compiled.
     * @throws JoeError on all runtime errors.
     */
    @Override
    public Object run(String filename, String source) throws SyntaxError, JoeError {
        var traces = new ArrayList<Trace>();

        Scanner scanner = new Scanner(filename, source, traces::add);
        List<Token> tokens = scanner.scanTokens();
        var buffer = scanner.buffer();
        Parser parser = new Parser(buffer, tokens, traces::add);
        var statements = parser.parse();

        // Stop if there was a syntax error.
        if (!traces.isEmpty()) {
            throw new SyntaxError("Syntax error in input, halting.", traces);
        }

        Resolver resolver = new Resolver(interpreter, traces::add);
        resolver.resolve(statements);

        // Stop if there was a resolution error.
        if (!traces.isEmpty()) {
            throw new SyntaxError("Syntax error in input, halting.", traces);
        }

        // Save the buffer, for later introspection.
        buffers.put(filename, buffer);

        return interpreter.interpret(statements);
    }


    @Override
    public boolean isComplete(String source) {
        var traces = new ArrayList<Trace>();

        Scanner scanner = new Scanner("*isComplete*", source, traces::add);
        List<Token> tokens = scanner.scanTokens();
        var buffer = scanner.buffer();
        Parser parser = new Parser(buffer, tokens, traces::add);
        var statements = parser.parse();

        // Stop now if there was a syntax error.
        if (!traces.isEmpty()) {
            return false;
        }

        Resolver resolver = new Resolver(interpreter, traces::add);
        resolver.resolve(statements);

        // Check for resolution errors.
        return traces.isEmpty();
    }

    @Override
    public boolean isCallable(Object callee) {
        return callee instanceof JoeCallable;
    }

    /**
     * Calls a JoeCallable value with the given arguments.
     * @param callee A Joe value which must be callable.
     * @param args The arguments to pass to the callable
     * @return The result of calling the callable.
     */
    @Override
    public Object call(Object callee, Object... args) {
        if (callee instanceof JoeCallable callable) {
            try {
                return callable.call(joe, new Args(args));
            } catch (JoeError ex) {
                var list = new ArrayList<>(List.of(args));
                list.add(0, callee);
                var arguments = list.stream()
                    .map(joe::stringify)
                    .collect(Collectors.joining(", "));
                throw ex
                    .addFrame("In " + callable.callableType() + " " +
                        callable.signature())
                    .addInfo("In java call(" + arguments + ")");
            }
        } else {
            throw joe.expected("callable", callee);
        }
    }

    @Override
    public String dump(String scriptName, String source) {
        var traces = new ArrayList<Trace>();

        Scanner scanner = new Scanner(scriptName, source, traces::add);
        List<Token> tokens = scanner.scanTokens();
        var buffer = scanner.buffer();
        Parser parser = new Parser(buffer, tokens, traces::add);
        var statements = parser.parse();

        // Stop if there was a syntax error.
        if (!traces.isEmpty()) {
            throw new SyntaxError("Syntax error in input, halting.", traces);
        }

        Resolver resolver = new Resolver(interpreter, traces::add);
        resolver.resolve(statements);

        // Stop if there was a resolution error.
        if (!traces.isEmpty()) {
            throw new SyntaxError("Syntax error in input, halting.", traces);
        }

        return new Dumper().dump(statements);
    }

    @Override
    public boolean isDebug() {
        return false;
    }

    @Override
    public void setDebug(boolean flag) {
        // Do nothing; there is no debugging code.
    }
}
