package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that implements the boilerplate for subclasses of native classes
 * that implement JoeValue (e.g., StackPaneInstance).  A JoeValueCore is a
 * component of instances of the subclass.
 */
public class JoeValueCore {
    private final static String TO_STRING = "toString";

    //-------------------------------------------------------------------------
    // Instance Variables

    private final JoeClass joeClass;

    private final Object host;
    private final Map<String,Object> fields = new HashMap<>();

    // Default "toString()" implementation.
    private final NativeCallable _toString;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Initializes the core.
     * @param joeClass The JoeClass of which the host is an instance.
     * @param host The JoeValue instance of which this is a component.
     */
    public JoeValueCore(JoeClass joeClass, Object host) {
        this.joeClass = joeClass;
        this.host = host;
        this._toString = new NativeMethod<>(this, "toString",
            (objc, joe, args) -> this.defaultToString());
    }

    //-------------------------------------------------------------------------
    // Object Method implementations

    /**
     * The script-level type of this object.
     * @return The type
     */
    public JoeType type() {
        return joeClass;
    }

    /**
     * Returns true if the object has the given field, and false otherwise.
     * Note: this checks for actual fields, not method properties.
     * @param name The field name
     * @return true or false
     */
    public boolean hasField(String name) {
        return fields.containsKey(name);
    }

    /**
     * Gets the names of the value's fields, or the empty list if
     * none.
     * @return The names.
     */
    public List<String> getFieldNames() {
        return new ArrayList<>(fields.keySet());
    }

    /**
     * Gets a field's value.
     * @param name The field name.
     * @return The value
     */
    public Object get(String name) {
        var value = fields.get(name);

        if (value == null) {
            value = joeClass.bind(host, name);
        }

        if (value != null) return value;

        if (name.equals(TO_STRING)) {
            return _toString;
        }

        throw new JoeError("Undefined property: '" + name + "'.");
    }

    /**
     * Sets a field's value.
     * @param name The field name
     * @param value The value
     */
    public void set(String name, Object value) {
        fields.put(name, value);
    }

    /**
     * Computes the string representation for the object, taking its
     * script-level toString() method into account.
     * @param joe The interpreter
     * @return The string
     */
    public String stringify(Joe joe) {
        var callable = get(TO_STRING);
        return (String)joe.call(callable);
    }

    /**
     * The default string representation.
     * @return the string.
     */
    public String defaultToString() {
        return type().name() + "@" + host.hashCode();
    }
}
