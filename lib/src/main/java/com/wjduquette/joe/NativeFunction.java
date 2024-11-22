package com.wjduquette.joe;

/**
 * A native function, implemented in Java, for use in a Joe interpreter.
 */
public class NativeFunction implements JoeCallable, HasTypeName {
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
        try {
            return joeLambda.call(joe, args);
        } catch (JoeError ex) {
            throw ex.addFrame(null,
                "In native " + kind + " " + signature());
        } catch (Exception ex) {
            throw new JoeError("Error in " + name + "(): " +
                ex.getMessage());
        }
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
    // HasTypeName API

    @Override
    public String typeName() {
        return "<native function>";
    }

    //-------------------------------------------------------------------------
    // Object API

    @Override
    public String toString() {
        return "<native fn " + name + ">";
    }
}
