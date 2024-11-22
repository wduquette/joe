package com.wjduquette.joe;

/**
 * A native function, implemented in Java, for use in a Joe interpreter.
 * @param <V> The value type
 */
public class NativeMethod<V> implements JoeCallable, HasTypeName {
    private final V value;
    private final String name;
    private final JoeValueLambda<V> valueLambda;

    /**
     * Creates a native function
     * @param value The bound value
     * @param name The function's name
     * @param valueLambda The function's callable, usually a method reference.
     */
    public NativeMethod(V value, String name, JoeValueLambda<V> valueLambda) {
        this.value = value;
        this.name = name;
        this.valueLambda = valueLambda;
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
            return valueLambda.call(value, joe, args);
        } catch (JoeError ex) {
            throw ex.addFrame(null,
                "In " + joe.typeName(value) + " method " +
                name() + "(" + joe.join(", ", args.asList()) + ")");
        } catch (Exception ex) {
            throw new JoeError("Error in " + name + "(): " +
                ex.getMessage());
        }
    }

    @Override
    public String callableType() {
        return "native method";
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
        return "<native method>";
    }

    //-------------------------------------------------------------------------
    // Object API
    @Override
    public String toString() {
        return "<native method " + name + ">";
    }
}
