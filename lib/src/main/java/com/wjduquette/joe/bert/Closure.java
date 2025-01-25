package com.wjduquette.joe.bert;

import com.wjduquette.joe.JoeObject;

/**
 * A Closure is a Bert `Function` wrapped up with its captured local variables.
 * The `Compiler` produces `Functions`, which includes methods; but at runtime
 * every `Function` is wrapped as a `Closure`.
 */
public class Closure implements BertCallable, JoeObject {
    //-------------------------------------------------------------------------
    // Instance Variables

    final Function function;
    final Upvalue[] upvalues;

    //-------------------------------------------------------------------------
    // Constructor

    Closure(Function function) {
        this.function = function;
        this.upvalues = new Upvalue[function.upvalueCount];
    }

    //-------------------------------------------------------------------------
    // JoeObject API

    @Override
    public String typeName() {
        return "<function>";
    }

    @Override
    public Object get(String name) {
        throw new UnsupportedOperationException(
            "Values of type <function> have no properties.");
    }

    @Override
    public void set(String name, Object value) {
        throw new UnsupportedOperationException(
            "Values of type <function> have no properties.");
    }

    //-------------------------------------------------------------------------
    // JoeCallable API

    @Override
    public String callableType() {
        return function.type().text();
    }

    @Override
    public boolean isScripted() {
        return true;
    }

    @Override
    public String signature() {
        return function.signature();
    }

    //-------------------------------------------------------------------------
    // Object API

    @Override
    public String toString() {
        return "<" + function.type().text() + " " + function.name() + ">";
    }
}
