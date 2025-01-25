package com.wjduquette.joe.bert;

import com.wjduquette.joe.*;

import java.util.HashMap;
import java.util.Map;

public class BertClass implements BertCallable, JoeClass, JoeObject {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The class name (and the name of its global variable)
    private final String name;

    // This class's immediate superclass, whether scripted or native, if any.
    // See inheritSuperclass() and bind() for details
    private JoeClass superclass = null;

    // This class's closest native ancestor class, if any.
    // See inheritSuperclass() and bind() for details
    private JoeClass nativeAncestor = null;

    // Static methods and constants
    final Map<String, Closure> staticMethods = new HashMap<>();
    private final Map<String, Object> fields = new HashMap<>();

    // The class's methods.
    final Map<String,Closure> methods = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor and building methods

    /**
     * Create the class object.  It is initially empty, having only a name;
     * later instructions will build up its content.
     * @param name The name
     */
    BertClass(String name) {
        this.name = name;
    }

    /**
     * Make this class inherit from the superclass.  If the superclass is a
     * Bert class, this class will copy its methods and inherit its native
     * ancestor.  Otherwise, the superclass is this class's superclass and
     * its native ancestor.  See bind().
     * @param superclass The superclass.
     */
    void inheritSuperclass(JoeClass superclass) {
        // FIRST, save the superclass.
        if (this.superclass != null) {
            throw new IllegalStateException(
                "Class '" + name + "' already has a superclass.");
        }

        this.superclass = superclass;

        if (superclass instanceof BertClass bertClass) {
            methods.putAll(bertClass.methods);
            nativeAncestor = bertClass.nativeAncestor;
        } else {
            nativeAncestor = superclass;
        }
    }


    //-------------------------------------------------------------------------
    // BertClass API

    public String name() {
        return name;
    }

    public JoeClass getSuperclass() {
        return superclass;
    }

    //-------------------------------------------------------------------------
    // JoeClass API

    @Override
    public JoeCallable bind(Object value, String name) {
        var method = methods.get(name);

        if (method != null) {
            return new BoundMethod(this, method);
        }

        if (nativeAncestor != null) {
            return nativeAncestor.bind(value, name);
        } else {
            return null;
        }
    }

    @Override
    public boolean canBeExtended() {
        return true;
    }

    @Override
    public JoeObject make(Joe joe, JoeClass joeClass) {
        if (superclass != null) {
            return superclass.make(joe, joeClass);
        } else {
            return new BertInstance(joeClass);
        }
    }

    //-------------------------------------------------------------------------
    // JoeObject API

    @Override
    public String typeName() {
        return "<class>";
    }

    @Override
    public Object get(String name) {
        if (fields.containsKey(name)) {
            return fields.get(name);
        }

        if (staticMethods.containsKey(name)) {
            return staticMethods.get(name);
        }

        throw new JoeError("Undefined property '" + name + "'.");
    }

    @Override
    public void set(String name, Object value) {
        fields.put(name, value);
    }

    @Override
    public String stringify(Joe joe) {
        return "<class " + name + ">";
    }

    //-------------------------------------------------------------------------
    // JoeCallable API

    @Override
    public String callableType() {
        return "class";
    }

    @Override
    public boolean isScripted() {
        return true;
    }

    @Override
    public String signature() {
        var init = methods.get(INIT);
        if (init != null) {
            return name + init.signature().substring(4);
        } else {
            return name + "()";
        }
    }

    //-------------------------------------------------------------------------
    // Object API

    @Override
    public String toString() {
        return "<class " + name + ">";
    }
}
