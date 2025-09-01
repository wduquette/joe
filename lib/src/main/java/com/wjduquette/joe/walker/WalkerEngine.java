package com.wjduquette.joe.walker;

import com.wjduquette.joe.*;
import com.wjduquette.joe.parser.ASTDumper;
import com.wjduquette.joe.parser.Parser;
import com.wjduquette.joe.parser.Stmt;
import com.wjduquette.joe.SourceBuffer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A Joe language engine that parses scripts to an Abstract Syntax Tree (AST)
 * and walks the AST to process/execute the script.
 */
public class WalkerEngine implements Engine {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The owning instance of Joe
    private final Joe joe;

    // Error traces accumulated during compilation
    private List<Trace> syntaxTraces = null;
    private boolean gotIncompleteScript = false;

    // The interpreter
    private final Interpreter interpreter;

    // The buffers
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<String, SourceBuffer> buffers = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an instance of the engine.
     * @param joe The Joe API
     */
    public WalkerEngine(Joe joe) {
        this.joe = joe;
        interpreter = new Interpreter(joe);
    }

    //-------------------------------------------------------------------------
    // Engine API

    @Override
    public Environment getEnvironment() {
        return interpreter.getEnvironment();
    }

    @Override
    public Environment getExports() {
        return interpreter.getExports();
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
        gotIncompleteScript = false;

        Parser parser = new Parser(buffer, this::reportError);
        var statements = parser.parseJoe();

        // Stop if there was a syntax error.
        if (!syntaxTraces.isEmpty()) {
            throw new SyntaxError("Syntax error in input, halting.",
                syntaxTraces, !gotIncompleteScript);
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
                list.addFirst(callee);
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
        return ASTDumper.dump(statements);
    }

    private void reportError(Trace trace, boolean incomplete) {
        syntaxTraces.add(trace);
        if (incomplete) {
            gotIncompleteScript = true;
        }
    }
}
