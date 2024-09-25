package com.wjduquette.joe;

/**
 * A native function, implemented in Java, for use in a Joe interpreter.
 */
public class NativeFunction implements JoeCallable {
    private final String name;
    private final String kind;
    private final JoeCallable callable;

    /**
     * Creates a native function
     * @param name The function's name
     * @param kind The function's kind: "function", "static method", "method",
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

    @Override
    public Object call(Joe joe, ArgQueue args) {
        try {
            return callable.call(joe, args);
        } catch (JoeError ex) {
            ex.getFrames().add("In native " + kind + " " + name() +
                "(" + joe.codify(args) + ")");
            throw ex;
        } catch (Exception ex) {
            throw new JoeError("Error in " + name + "(): " +
                ex.getMessage());
        }
    }

    @Override
    public String toString() {
        return "<native fn " + name + ">";
    }
}
