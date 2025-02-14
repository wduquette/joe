package com.wjduquette.joe.walker;

import com.wjduquette.joe.*;

import java.util.*;
import java.util.stream.Collectors;

public class WalkerEngine implements Engine {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The owning instance of Joe
    private final Joe joe;

    // Error traces accumulated during compilation
    private List<Trace> syntaxTraces = null;
    private boolean gotIncompleteError = false;

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
     * @param scriptName The source of the input.
     * @param source The input
     * @return The script's result
     * @throws SyntaxError if the script could not be compiled.
     * @throws JoeError on all runtime errors.
     */
    @Override
    public Object run(String scriptName, String source) throws SyntaxError, JoeError {
        var buffer = new SourceBuffer(scriptName, source);
        var statements = parseAndResolve(buffer);

        // Save the buffer, for later introspection.
        buffers.put(scriptName, buffer);

        try {
            return interpreter.interpret(statements);
        } catch (JoeError ex) {
            ex.addFrame("In <script>");
            throw ex;
        } catch (Return ex) {
            return ex.value;
        }
    }

    private List<Stmt> parse(SourceBuffer buffer) throws SyntaxError {
        syntaxTraces = new ArrayList<>();
        gotIncompleteError = false;

        Scanner scanner = new Scanner(buffer, this::reportError);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(buffer, tokens, this::reportError);
        var statements = parser.parse();

        // Stop if there was a syntax error.
        if (!syntaxTraces.isEmpty()) {
            throw new SyntaxError("Syntax error in input, halting.",
                syntaxTraces, !gotIncompleteError);
        }

        return statements;
    }

    private List<Stmt> parseAndResolve(SourceBuffer buffer) {
        var statements = parse(buffer);

        Resolver resolver = new Resolver(interpreter, syntaxTraces::add);
        resolver.resolve(statements);

        // Stop if there was a resolution error.
        if (!syntaxTraces.isEmpty()) {
            throw new SyntaxError("Syntax error in input, halting.",
                syntaxTraces, true);
        }

        return statements;
    }

    @Override
    public boolean isComplete(String source) {
        try {
            // If it parsed without error, it's complete.  It might
            // also have resolution errors, but that's irrelevant.
            parse(new SourceBuffer("*isComplete*", source));
            return true;
        } catch (SyntaxError ex) {
            // If there's an error, return the error's complete flag.
            return ex.isComplete();
        }
    }

    @Override
    public boolean isCallable(Object callee) {
        return callee instanceof NativeCallable;
    }

    /**
     * Calls a JoeCallable value with the given arguments.
     * @param callee A Joe value which must be callable.
     * @param args The arguments to pass to the callable
     * @return The result of calling the callable.
     */
    @Override
    public Object call(Object callee, Object... args) {
        if (callee instanceof NativeCallable callable) {
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
            } catch (Exception ex) {
                throw new UnexpectedError(null,
                    "Unexpected Java error: " + ex, ex);
            }
        } else {
            throw joe.expected("callable", callee);
        }
    }

    @Override
    public String dump(String scriptName, String source) {
        var buffer = new SourceBuffer(scriptName, source);
        var statements = parseAndResolve(buffer);
        return new Dumper().dump(statements);
    }

    private void reportError(Trace trace, boolean incomplete) {
        syntaxTraces.add(trace);
        if (incomplete) {
            gotIncompleteError = true;
        }
    }
}
