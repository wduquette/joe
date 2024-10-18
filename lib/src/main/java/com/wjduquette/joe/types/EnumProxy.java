package com.wjduquette.joe.types;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.TypeProxy;

import java.util.Collections;

/**
 * A generic proxy for enum types.  Each enum will need its own proxy.
 * @param <E> The enum type
 */
public class EnumProxy<E extends Enum<E>> extends TypeProxy<E> {
    //-------------------------------------------------------------------------
    // Static Methods

    public static <E extends Enum<E>> E valueOf(Class<E> cls, String name) {
        for (var c : cls.getEnumConstants()) {
            if (c.name().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    private final Class<E> cls;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an enum proxy.
     * @param name  The type's name in Joe code
     * @param cls The enum class
     */
    public EnumProxy(String name, Class<E> cls) {
        super(name);
        this.cls = cls;
        proxies(cls);

        // NEXT, define the enum values as constants of this type.
        for (var e : cls.getEnumConstants()) {
            constant(e.name(), e);
        }

        // Static Methods
        staticMethod("values",  this::_values);
        staticMethod("valueOf", this::_valueOf);

        // Methods
        method("name",     this::_name);
        method("ordinal",  this::_ordinal);
        method("toString", this::_toString);
    }

    //-------------------------------------------------------------------------
    // Static Methods

    private Object _values(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, getTypeName() + ".values()");
        var list = new ListValue();
        Collections.addAll(list, cls.getEnumConstants());
        return list;
    }

    private Object _valueOf(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, getTypeName() + ".valueOf(name)");
        var name = joe.toString(args.next());

        var c = EnumProxy.valueOf(cls, name);

        if (c != null) {
            return c;
        } else {
            throw joe.expected("name of " + getTypeName() + " constant", name);
        }
    }

    //-------------------------------------------------------------------------
    // Methods

    private Object _name(E value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "name()");
        return value.name();
    }

    private Object _ordinal(E value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "ordinal()");
        return (double)value.ordinal();
    }

    private Object _toString(E value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "toString()");
        return value.toString();
    }
}
