package com.wjduquette.joe.walker;

import com.wjduquette.joe.*;

import java.util.HashMap;
import java.util.Map;
import com.wjduquette.joe.SourceBuffer.Span;
import com.wjduquette.joe.types.ListValue;
import com.wjduquette.joe.types.TypeType;

/**
 * A class defined in a Joe script.
 */
class WalkerClass implements JoeClass, JoeValue, NativeCallable {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The class name
    private final String name;

    // The class's span in the source code.
    private final Span classSpan;

    // The superclass, or null
    private final JoeClass superclass;

    // Static methods and constants
    private final Map<String, WalkerFunction> staticMethods;
    private final Map<String, Object> fields = new HashMap<>();

    // JoeInstance Instance Methods
    private final Map<String, WalkerFunction> methods;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new class.
     * @param name The class's variable name.
     * @param superclass The superclass, or null
     * @param methods The map of methods by name
     */
    WalkerClass(
        String name,
        Span classSpan,
        JoeClass superclass,
        Map<String, WalkerFunction> staticMethods,
        Map<String, WalkerFunction> methods
    ) {
        this.name = name;
        this.classSpan = classSpan;
        this.superclass = superclass;
        this.staticMethods = staticMethods;
        this.methods = methods;
    }

    //-------------------------------------------------------------------------
    // WalkerClass API

    @SuppressWarnings("unused")
    public Span classSpan() {
        return classSpan;
    }

    public Object call(Joe joe, Args args) {
        JoeValue instance = make(joe, this);
        var initializer = (WalkerFunction)bind(instance, INIT);
        if (initializer != null) {
            try {
                initializer.call(joe, args);
            } catch (JoeError ex) {
                throw ex.addPendingFrame(initializer.span(),
                    "In initializer " + initializer.signature());
            }
        }
        return instance;
    }

    //-------------------------------------------------------------------------
    // JoeClass API


    @Override
    public JoeValue make(Joe joe, JoeClass joeClass) {
        if (superclass != null) {
            return superclass.make(joe, joeClass);
        } else {
            return new WalkerInstance(joeClass);
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public JoeCallable bind(Object value, String name) {
        var method = methods.get(name);

        if (method != null) {
            return method.bind((JoeValue)value);
        }

        if (superclass != null) {
            return superclass.bind(value, name);
        }

        return null;
    }

    @Override
    public boolean canBeExtended() {
        return true;
    }

    //-------------------------------------------------------------------------
    // JoeCallable API

    @Override
    public String callableType() {
        return "class";
    }

    @Override
    public String signature() {
        var method = methods.get(INIT);
        if (method == null) {
            return name + "()";
        } else {
            return name + method.signature().substring(INIT.length());
        }
    }

    @Override
    public boolean isScripted() {
        return true;
    }

    //-------------------------------------------------------------------------
    // JoeValue API

    @Override
    public JoeType type() {
        return TypeType.TYPE;
    }

    @Override
    public String typeName() {
        return "<class>";
    }

    @Override
    public boolean hasField(String name) {
        return fields.containsKey(name);
    }

    @Override
    public JoeList getFieldNames() {
        return new ListValue(fields.keySet());
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

    //-------------------------------------------------------------------------
    // Object API

    @Override
    public String toString() {
        return "<class " + name + ">";
    }
}
