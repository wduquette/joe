package com.wjduquette.joe.bert;

public class Closure {
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
