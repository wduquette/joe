package com.wjduquette.joe.bert;

import com.wjduquette.joe.*;

import java.util.Set;

/**
 * The Bert byte-code engine.
 */
public class BertEngine implements Engine {
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
    public BertEngine(Joe joe) {
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
            || callee instanceof BertCallable;
    }

    @Override
    public String dump(String filename, String source) throws SyntaxError {
        var compiler = new Compiler(joe);
        return compiler.dump(filename, source);
    }

    @Override
    public boolean isComplete(String source) {
        var compiler = new Compiler(joe);
        try {
            compiler.compile("*isComplete*", source);
            return true;
        } catch (SyntaxError ex) {
            return ex.isComplete();
        }
    }
}
