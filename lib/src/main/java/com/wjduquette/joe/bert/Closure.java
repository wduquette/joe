package com.wjduquette.joe.bert;

/**
 * A Closure is a Bert `Function` wrapped up with its captured local variables.
 * The `Compiler` produces `Functions`, which includes methods; but at runtime
 * every `Function` is wrapped as a `Closure`.
 */
public class Closure implements BertCallable {
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
    // Methods

    @Override
    public String toString() {
        return "<" + function.type().text() + " " + function.name() + ">";
    }
}
