package com.wjduquette.joe.clark;

import com.wjduquette.joe.*;

/**
 * The Bert byte-code engine.
 */
public class ClarkEngine implements Engine {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    private final VirtualMachine vm;

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
    public Environment getEnvironment() {
        return vm.getEnvironment();
    }

    @Override
    public Environment getExports() {
        return vm.getExports();
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
}
