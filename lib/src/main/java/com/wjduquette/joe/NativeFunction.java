package com.wjduquette.joe;

/**
 * A native function, implemented in Java, for use in a Joe interpreter.
 */
public class NativeFunction implements JoeCallable, HasTypeName {
    private final String name;
    private final String kind;
    private final JoeCallable callable;

    /**
     * Creates a native function
     * @param name The function's name
     * @param kind The function's kind: "function", "static method",
     *             "initializer"
     * @param callable The function's callable, usually a method reference.
     */
    public NativeFunction(String name, String kind, JoeCallable callable) {
        this.name = name;
        this.kind = kind;
        this.callable = callable;
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
            return callable.call(joe, args);
        } catch (JoeError ex) {
            throw ex.addInfo("In native " + kind + " " + name() +
                "(" + joe.join(", ", args.asList()) + ")");
        } catch (Exception ex) {
            throw new JoeError("Error in " + name + "(): " +
                ex.getMessage());
        }
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
