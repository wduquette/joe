package com.wjduquette.joe;

/**
 * A native function, implemented in Java, for use in a Joe interpreter.
 * @param <V> The value type
 */
public class NativeMethod<V> implements JoeCallable, HasTypeName {
    private final V value;
    private final String name;
    private final JoeValueCallable<V> callable;

    /**
     * Creates a native function
     * @param value The bound value
     * @param name The function's name
     * @param callable The function's callable, usually a method reference.
     */
    public NativeMethod(V value, String name, JoeValueCallable<V> callable) {
        this.value = value;
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

    //-------------------------------------------------------------------------
    // JoeCallable API

    @Override
    public Object call(Joe joe, Args args) {
        try {
            return callable.call(value, joe, args);
        } catch (JoeError ex) {
            ex.getFrames().add("In " + joe.typeName(value) + " method " +
                name() + "(" + joe.codify(args) + ")");
            throw ex;
        } catch (Exception ex) {
            throw new JoeError("Error in " + name + "(): " +
                ex.getMessage());
        }
    }

    //-------------------------------------------------------------------------
    // HasTypeName API

    @Override
    public String typeName() {
        return "<native method>";
    }

    //-------------------------------------------------------------------------
    // Object API
    @Override
    public String toString() {
        return "<native method " + name + ">";
    }
}
