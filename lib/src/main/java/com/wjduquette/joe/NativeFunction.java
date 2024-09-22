package com.wjduquette.joe;

/**
 * A native function, implemented in Java, for use in a Joe interpreter.
 */
public class NativeFunction implements JoeCallable {
    private final String name;
    private final JoeCallable callable;

    /**
     * Creates a native function
     * @param name The function's name
     * @param callable The function's callable, usually a method reference.
     */
    public NativeFunction(String name, JoeCallable callable) {
        this.name = name;
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
            // TODO: Add stack frame
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
