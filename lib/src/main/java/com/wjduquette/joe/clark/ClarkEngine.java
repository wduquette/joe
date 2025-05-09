package com.wjduquette.joe.clark;

import com.wjduquette.joe.*;
import com.wjduquette.joe.parser.Parser;
import com.wjduquette.joe.parser.Stmt;
import com.wjduquette.joe.scanner.SourceBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The Bert byte-code engine.
 */
public class ClarkEngine implements Engine {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    private final VirtualMachine vm;

    // Error traces accumulated during parse()
    private List<Trace> syntaxTraces = null;
    private boolean gotIncompleteScript = false;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an instance of BertEngine for the owning Joe interpreter.
     * @param joe The interpreter.
     */
    public ClarkEngine(Joe joe) {
        this.joe = joe;
        this.vm = new VirtualMachine(joe);
    }

    //-------------------------------------------------------------------------
    // Engine API

    @Override
    public Set<String> getVarNames() {
        return vm.getVarNames();
    }

    @Override
    public Object getVar(String name) {
        return vm.getVar(name);
    }

    @Override
    public void setVar(String name, Object value) {
        vm.setVar(name, value);
    }

    @Override
    public Object run(String scriptName, String source)
        throws JoeError
    {
        return vm.interpret(scriptName, source);
    }

    @Override
    public Object call(Object callee, Object... args) {
        return vm.callFromJava(callee, args);
    }

    @Override
    public boolean isCallable(Object callee) {
        return callee instanceof NativeCallable
            || callee instanceof ClarkCallable;
    }

    @Override
    public String dump(String filename, String source) throws SyntaxError {
        var compiler = new Compiler(joe);
        return compiler.dump(filename, source);
    }

    @Override
    public boolean isComplete(String source) {
        try {
            // If it parsed without error, it's complete.  It might
            // also have resolution errors, but that's irrelevant.
            parse(new SourceBuffer("*isComplete*", source));
            return true;
        } catch (SyntaxError ex) {
            return ex.isComplete();
        }
    }

    private List<Stmt> parse(SourceBuffer buffer) throws SyntaxError {
        syntaxTraces = new ArrayList<>();
        gotIncompleteScript = false;

        Parser parser = new Parser(buffer, this::reportError);
        var statements = parser.parse();

        // Stop if there was a syntax error.
        if (!syntaxTraces.isEmpty()) {
            var err = new SyntaxError("Syntax error in input, halting.",
                syntaxTraces, !gotIncompleteScript);
            syntaxTraces = null;
            throw err;
        }

        return statements;
    }

    private void reportError(Trace trace, boolean incomplete) {
        syntaxTraces.add(trace);
        if (incomplete) {
            gotIncompleteScript = true;
        }
    }
}
