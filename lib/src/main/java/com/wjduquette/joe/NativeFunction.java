package com.wjduquette.joe;

public class NativeFunction implements JoeCallable {
    private final String name;
    private final JoeCallable callable;

    public NativeFunction(String name, JoeCallable callable) {
        this.name = name;
        this.callable = callable;
    }

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
