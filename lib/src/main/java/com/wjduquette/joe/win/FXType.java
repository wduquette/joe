package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import javafx.beans.property.Property;

import java.util.HashMap;
import java.util.Map;

/**
 * A ProxyType that includes support for JavaFX properties.  The documentation
 * for the related methods is defined as a JoeDoc @mixin; all direct subclasses
 * (e.g., NodeType, MenuItemType) should "@includeMixin FXType".
 * @param <V> The proxied type
 */
public class FXType<V> extends ProxyType<V> {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Map<Keyword, PropertyDef<V,?>> properties =
        new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @mixin FXType

    /**
     * Creates an FXType for the given type.
     * @param name The script-level type name.
     */
    public FXType(String name) {
        super(name);

        method("getProperty", this::_getProperty);
        method("listenTo",    this::_listenTo);
        method("properties",  this::_properties);
        method("setProperty", this::_setProperty);
        method("toString",    this::_toString);
    }

    //-------------------------------------------------------------------------
    // Overridable methods

    /**
     * Returns a stringified value, i.e., a value for display.
     * Defaults to the value's toString().
     * @param joe The engine
     * @param value The value
     * @return The string
     */
    public String stringify(Joe joe, Object value) {
        return name() + "@" + value.hashCode();
    }

    //-------------------------------------------------------------------------
    // Builder API

    /**
     * This proxy inherits not only the superProxy's methods but also
     * an FXType properties.
     * @param superProxy The supertype's proxy
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void extendsProxy(ProxyType<? super V> superProxy) {
        super.extendsProxy(superProxy);
        if (superProxy instanceof FXType fxType) {
            properties.putAll(fxType.properties);
        }
    }

    /**
     * Defines a JavaFX property.
     * @param keywordName The identifying keyword's name.
     * @param getter The getter for the property object.
     * @param converter The converter for the value.
     * @param <P> The property's value type.
     */
    public <P> void fxProperty(
        String keywordName,
        PropertyGetter<V,P> getter,
        ArgConverter<P> converter
    ) {
        var keyword = new Keyword(keywordName);
        var def = new PropertyDef<V,P>(keyword, null, getter, converter);
        properties.put(keyword, def);
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method getProperty
    // @args keyword
    // @result value
    // Gets the value of the property with the given *keyword*.
    private Object _getProperty(V obj, Joe joe, Args args) {
        args.exactArity(1, "getProperty(keyword)");

        return toDef(joe, args.next()).getProperty(obj).getValue();
    }

    //**
    // @method listenTo
    // @args keyword, listener
    // @result this
    // Adds a *listener* to the property with the given *keyword*.
    // The listener should be a callable taking the three arguments:
    //
    // - The property keyword
    // - The old value of the property
    // - The new value of the property
    //
    // The listener will be called when the property's value changes.
    private Object _listenTo(V obj, Joe joe, Args args) {
        args.exactArity(2, "listenTo(keyword, listener");
        var keyword = joe.toKeyword(args.next());
        var def =  toDef(joe, keyword).getProperty(obj);
        var handler = args.next();

        def.addListener((p,o,n) -> joe.call(handler, keyword, o, n));
        return obj;
    }

    //**
    // @method properties
    // @result joe.Set
    // Returns a readonly `Set` of the object's property keywords.
    private Object _properties(V obj, Joe joe, Args args) {
        args.exactArity(0, "properties()");
        return joe.readonlySet(properties.keySet());
    }

    //**
    // @method setProperty
    // @args keyword, value
    // @result this
    // Sets the *value* of the property with the given *keyword*.
    // The *value* must be assignable to the property's value type.
    private Object _setProperty(V obj, Joe joe, Args args) {
        args.exactArity(2, "setProperty(keyword, value)");

        var def = toDef(joe, args.next());
        def.setProperty(joe, obj, args.next());
        return obj;
    }


    //**
    // @method toString
    // @result joe.String
    // Returns the value's string representation.
    private Object _toString(V obj, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return stringify(joe, obj);
    }

    //-------------------------------------------------------------------------
    // Utilities

    private PropertyDef<V,?> toDef(Joe joe, Object arg) {
        var def = properties.get(joe.toKeyword(arg));

        if (def == null) {
            throw joe.expected("property keyword", arg);
        }

        return def;
    }

    //-------------------------------------------------------------------------
    // Helper Classes

    /**
     * A functional interface for retrieving a JavaFX property from an
     * object.
     * @param <V> The object type
     * @param <P> The property type
     */
    public interface PropertyGetter<V, P> {
        /**
         * Gets the value of an object property.
         * @param value The target object
         * @return The property value
         */
        Property<P> get(V value);
    }

    /**
     * A functional interface for converting an arbitrary Object to a
     * value of property type P.  The converter is expected to throw a
     * JoeError on conversion failure.
     * @param <P> The property type.
     */
    public interface ArgConverter<P> {
        /**
         * Converts the argument to a value of type P.
         * @param joe The interpreter
         * @param arg The argument
         * @return The converted value
         * @throws JoeError on conversion failure.
         */
        P convert(Joe joe, Object arg);
    }

    private record PropertyDef<V, P>(
        Keyword keyword,
        Class<P> propertyClass,
        PropertyGetter<V,P> getter,
        ArgConverter<P> converter
    ) {
        Property<P> getProperty(V obj) {
            return getter.get(obj);
        }

        void setProperty(Joe joe, V obj, Object arg) {
            var property = getProperty(obj);

            try {
                property.setValue(converter.convert(joe, arg));
            } catch (Exception ex) {
                throw new JoeError(
                    "Failed to set property:" + ex.getMessage());
            }
        }
    }
}
