package com.wjduquette.joe.bert;

/**
 * Closure is the {@link VirtualMachine}'s representation for compiled
 * functions.
 *
 * <p>
 * The {@link Compiler} produces a {@link Function} object for each scripted
 * function.  The Function is then compiled into its parent function using
 * the {@code CLOSURE} instruction, which includes details about the variables
 * the function closes over.  {@code CLOSURE} builds all this into a Closure.
 * </p>
 *
 * <p>Closures are built in this roundabout way because a single Function
 * can be used to create any number of Closures, each of which closes over
 * its own set of {@link Upvalue Upvalues}.</p>
 */
public class Closure implements BertCallable {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The compiled function
    final Function function;

    // The closure's Upvalues
    final Upvalue[] upvalues;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a closure from a function.
     * @param function The function.
     */
    Closure(Function function) {
        this.function = function;
        this.upvalues = new Upvalue[function.upvalueCount];
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
        return "<" + function.type().text() + " " + signature() + ">";
    }
}
