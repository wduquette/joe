package com.wjduquette.joe.bert;

import com.wjduquette.joe.Engine;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.SyntaxError;

import java.util.Set;

// Temporary main program.  Later this will be `BertEngine`, and we will
// invoke it from the main app.
public class BertEngine implements Engine {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    private final VirtualMachine vm;
    private boolean debug = false;

    //-------------------------------------------------------------------------
    // Constructor

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
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean isCallable(Object callee) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String dump(String filename, String source) throws SyntaxError {
        var compiler = new Compiler(joe);

        // Dumps to System.out at present.
        compiler.compile(filename, source);
        return null;
    }

    @Override
    public boolean isComplete(String source) {
        // TODO: Implement properly
        return true;
    }
}
