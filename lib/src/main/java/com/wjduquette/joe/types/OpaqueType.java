package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.ProxyType;

/**
 * The type proxy for opaque native types.
 */
public class OpaqueType extends ProxyType<Object> {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Class<?> javaClass;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an instance of the proxy.
     */
    public OpaqueType(Class<?> javaClass) {
        super(javaClass.getSimpleName());
        this.javaClass = javaClass;

        //**
        // @package joe
        // @type Opaque
        // `Opaque` is the "type" of all opaque Java values: the fallback when
        // Joe has no other type information for a value.  Thus, this page
        // describes the interface of the type returned by
        //
        // ```joe
        // var type = Joe.typeOf(someOpaqueValue);
        // ```
        //
        // **Note**: there is no `Opaque` type defined in the global scope;
        // `Joe.typeOf()` will return a distinct opaque type for each
        // distinct Java class with no explicit Joe binding.
        staticType();

        //**
        // @method name
        // @result String
        // Returns the simple name of the Java class to which the opaque
        // value belongs.  In Java terms:
        //
        // ```java
        // return value.getClass().getSimpleName();
        // ```
        staticMethod("name", this::_name);

        //**
        // @method javaName
        // @result String
        // Returns the full name of the Java class to which the opaque
        // value belongs.  In Java terms:
        //
        // ```java
        // return value.getClass().getName();
        // ```
        staticMethod("javaName", this::_javaName);

        //**
        // @method toString
        // @result String
        // Returns the same string as the `javaName()` method.
        method("toString",   this::_toString);
    }

    //-------------------------------------------------------------------------
    // Static Methods

    private Object _name(Joe joe, Args args) {
        args.exactArity(0, "name()");
        return name();
    }

    private Object _javaName(Joe joe, Args args) {
        args.exactArity(0, "javaName()");
        return javaClass.getName();
    }

    //-------------------------------------------------------------------------
    // Instance Methods

    private Object _toString(Object value, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return javaClass.getName();
    }
}
