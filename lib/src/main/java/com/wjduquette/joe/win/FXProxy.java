package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import javafx.beans.property.Property;

import java.util.HashMap;
import java.util.Map;

public class FXProxy<V> extends TypeProxy<V> {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Map<Keyword, PropertyDef<V,?>> properties =
        new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type FX
    // Type used to define JavaFX type proxies.
    public FXProxy(String name) {
        super(name);

        method("getProperty", this::_getProperty);
        method("listenTo",    this::_listenTo);
        method("properties",  this::_properties);
        method("setProperty", this::_setProperty);
    }

    //-------------------------------------------------------------------------
    // Builder API

    /**
     * This proxy inherits not only the superProxy's methods but also
     * an FXProxy properties.
     * @param superProxy The supertype's proxy
     */
    public void extendsProxy(TypeProxy<? super V> superProxy) {
        super.extendsProxy(superProxy);
        if (superProxy instanceof FXProxy fxProxy) {
            properties.putAll(fxProxy.properties);
        }
    }

    /**
     * Defines a JavaFX property.
     * @param keywordName The identifying keyword's name.
     * @param cls The property's value class.
     * @param getter The getter for the property object.
     * @param <P> The property's value type.
     */
    public <P> void fxProperty(
        String keywordName,
        Class<P> cls,
        PropertyGetter<V,P> getter
    ) {
        var keyword = new Keyword(keywordName);
        var def = new PropertyDef<V,P>(keyword, cls, getter);
        properties.put(keyword, def);
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method getProperty
    // @args keyword
    // @result value
    // Gets the value of the property with the given *keyword*.
    private Object _getProperty(V obj, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "getProperty(keyword)");

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
    private Object _listenTo(V obj, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 2, "listenTo(keyword, listener");
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
    private Object _properties(V obj, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "properties()");
        return joe.readonlySet(properties.keySet());
    }

    //**
    // @method setProperty
    // @args keyword, value
    // @result this
    // Sets the *value* of the property with the given *keyword*.
    // The *value* must be assignable to the property's value type.
    private Object _setProperty(V obj, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 2, "setProperty(keyword, value)");

        var def = toDef(joe, args.next());
        def.setProperty(joe, obj, args.next());
        return obj;
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

    public interface PropertyGetter<V, P> {
        Property<P> get(V value);
    }

    private record PropertyDef<V, P>(
        Keyword keyword,
        Class<P> propertyClass,
        PropertyGetter<V,P> getter
    ) {
        Property<P> getProperty(V obj) {
            return getter.get(obj);
        }

        // Note: the cast to (P) is checked by the isAssignableFrom test.
        @SuppressWarnings("unchecked")
        void setProperty(Joe joe, V obj, Object value) {
            var property = getProperty(obj);
            if (!propertyClass.isAssignableFrom(value.getClass())) {
                throw joe.expected("valid property value", value);
            }

            try {
                property.setValue((P)value);
            } catch (Exception ex) {
                throw new JoeError(
                    "Failed to set property:" + ex.getMessage());
            }
        }
    }
}
