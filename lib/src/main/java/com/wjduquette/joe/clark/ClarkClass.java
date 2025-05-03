package com.wjduquette.joe.clark;

import com.wjduquette.joe.*;
import com.wjduquette.joe.types.TypeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BertClass is the internal representation for a scripted Joe class.
 */
public class ClarkClass implements JoeClass, JoeValue, ClarkType, ClarkCallable {
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
    final Map<String, Closure> methods = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor and building methods

    /**
     * Creates the class object.  It is initially empty, having only a name;
     * the compiled class declaration builds it up over a number of
     * instructions.
     * @param name The name
     */
    ClarkClass(String name) {
        this.name = name;
    }

    /**
     * Makes this class inherit from its superclass.  If the superclass is a
     * Bert class, this class will copy its methods and inherit its native
     * ancestor.  Otherwise, the superclass is this class's superclass and
     * also its native ancestor.  See {@code bind()}
     * @param superclass The superclass.
     */
    void inheritSuperclass(JoeClass superclass) {
        // FIRST, save the superclass.
        if (this.superclass != null) {
            throw new IllegalStateException(
                "Class '" + name + "' already has a superclass.");
        }

        this.superclass = superclass;

        if (superclass instanceof ClarkClass clarkClass) {
            methods.putAll(clarkClass.methods);
            nativeAncestor = clarkClass.nativeAncestor;
        } else {
            nativeAncestor = superclass;
        }
    }

    public void addMethod(String name, Closure closure) {
        methods.put(name, closure);
    }

    public void addStaticMethod(String name, Closure closure) {
        staticMethods.put(name, closure);
    }

    //-------------------------------------------------------------------------
    // JoeType API

    @Override
    public String name() {
        return name;
    }

    @Override
    public JoeType supertype() {
        return superclass;
    }

    //-------------------------------------------------------------------------
    // JoeClass API

    @Override
    public JoeCallable bind(Object value, String name) {
        var method = methods.get(name);

        if (method != null) {
            return new BoundMethod(value, method);
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
    public JoeValue make(Joe joe, JoeClass joeClass) {
        if (superclass != null) {
            return superclass.make(joe, joeClass);
        } else {
            return new ClarkInstance(joeClass);
        }
    }

    //-------------------------------------------------------------------------
    // JoeValue API

    @Override
    public JoeType type() {
        return TypeType.TYPE;
    }

    @Override
    public boolean hasField(String name) {
        return fields.containsKey(name);
    }

    @Override
    public List<String> getFieldNames() {
        return new ArrayList<>(fields.keySet());
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
