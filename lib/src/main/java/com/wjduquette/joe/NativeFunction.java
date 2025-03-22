package com.wjduquette.joe;

/**
 * A native function, implemented in Java, for use in a Joe interpreter.
 */
public class NativeFunction implements NativeCallable {
    private final String name;
    private final String kind;
    private final JoeLambda joeLambda;

    /**
     * Creates a native function
     * @param name The function's name
     * @param kind The function's kind: "function", "static method",
     *             "initializer"
     * @param joeLambda The function's callable, usually a method reference.
     */
    public NativeFunction(String name, String kind, JoeLambda joeLambda) {
        this.name = name;
        this.kind = kind;
        this.joeLambda = joeLambda;
    }

    /**
     * Gets the function's name.
     * @return The name
     */
    public String name() {
        return name;
    }

    //-------------------------------------------------------------------------
    // JoeCallable API

    @Override
    public Object call(Joe joe, Args args) {
        return joeLambda.call(joe, args);
    }

    @Override
    public String callableType() {
        return "native " + kind;
    }

    @Override
    public String signature() {
        return name() + "(...)";
    }

    @Override
    public boolean isScripted() {
        return false;
    }

    //-------------------------------------------------------------------------
    // Object API

    @Override
    public String toString() {
        return "<native " + kind + " " + name + ">";
    }
}
