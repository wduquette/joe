package com.wjduquette.joe;

import java.util.HashMap;
import java.util.Map;

/**
 * A class defined in a Joe script.
 */
class ScriptedClass implements JoeClass, JoeObject {
    public static final String INIT = "init";

    //-------------------------------------------------------------------------
    // Instance Variables

    // The class name
    private final String name;

    // The superclass, or null
    private final JoeClass superclass;

    // Static methods and constants
    private final Map<String, JoeFunction> staticMethods;
    private final Map<String, Object> fields = new HashMap<>();

    // JoeInstance Instance Methods
    private final Map<String, JoeFunction> methods;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new class.
     * @param name The class's variable name.
     * @param superclass The superclass, or null
     * @param methods The map of methods by name
     */
    ScriptedClass(
        String name,
        JoeClass superclass,
        Map<String, JoeFunction> staticMethods,
        Map<String, JoeFunction> methods
    ) {
        this.name = name;
        this.superclass = superclass;
        this.staticMethods = staticMethods;
        this.methods = methods;
    }

    //-------------------------------------------------------------------------
    // JoeClass API

    @Override
    public String name() {
        return name;
    }

    @Override
    public JoeFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if (superclass != null) {
            return superclass.findMethod(name);
        }

        return null;
    }

    @Override
    public Object call(Joe joe, Args args) {
        JoeInstance instance = new JoeInstance(this);
        JoeFunction initializer = findMethod(INIT);
        if (initializer != null) {
            initializer.bind(instance).call(joe, args);
        }
        return instance;
    }

    @Override
    public boolean canSubclass() {
        return true;
    }

    //-------------------------------------------------------------------------
    // JoeObject API

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
