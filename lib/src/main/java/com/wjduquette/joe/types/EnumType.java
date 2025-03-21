package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.ProxyType;

import java.util.Collections;

/**
 * A generic proxy for enum types.  Each enum will need its own proxy.
 *
 * <p><b>NOTE:</b> if this proxy is changed, update the default docs added
 * by the `@enum` tag in `tools.doc.DocCommentParser`.</p>
 * @param <E> The enum type
 */
public class EnumType<E extends Enum<E>> extends ProxyType<E> {
    //-------------------------------------------------------------------------
    // Static Methods

    /**
     * Given a string, returns the matching enum constant, or null if none.
     * The name match is case-insensitive.
     * @param cls The enum type's Class.
     * @param name The constant name
     * @return The constant
     * @param <E> The enum type
     */
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
    public EnumType(String name, Class<E> cls) {
        super(name);
        this.cls = cls;
        proxies(cls);

        // NEXT, define the enum values as constants of this type.
        for (var e : cls.getEnumConstants()) {
            constant(e.name(), e);
        }

        // Initializer
        initializer(this::_initializer);

        // Static Methods
        staticMethod("values",  this::_values);

        // Methods
        method("name",     this::_name);
        method("ordinal",  this::_ordinal);
        method("toString", this::_toString);
    }

    //-------------------------------------------------------------------------
    // Initializer

    private Object _initializer(Joe joe, Args args) {
        args.exactArity(1, name() + "(value)");
        return joe.toEnum(args.next(), cls);
    }

    //-------------------------------------------------------------------------
    // Static Methods

    private Object _values(Joe joe, Args args) {
        args.exactArity(0, name() + ".values()");
        var list = new ListValue();
        Collections.addAll(list, cls.getEnumConstants());
        return list;
    }


    //-------------------------------------------------------------------------
    // Methods

    private Object _name(E value, Joe joe, Args args) {
        args.exactArity(0, "name()");
        return value.name();
    }

    private Object _ordinal(E value, Joe joe, Args args) {
        args.exactArity(0, "ordinal()");
        return (double)value.ordinal();
    }

    private Object _toString(E value, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return value.toString();
    }
}
