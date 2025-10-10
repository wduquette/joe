package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;

import java.util.HashMap;
import java.util.Map;

/**
 * A base ProxyType for JavaFX widgets, including support for JavaFX
 * properties.  Proxies for JavaFX types like Node and MenuItem
 * should use subclass WidgetType and use the {@code @extends Widget}
 * JoeDoc tag.
 * @param <W> The proxied widget type
 */
public class WidgetType<W> extends ProxyType<W> {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Map<Keyword, PropertyDef<W,?>> properties =
        new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @widget Widget
    // `Widget` is the base class for JavaFX widgets as represented in Joe:
    // effectively, all JavaFX types that have JavaFX properties.  There is
    // no direct equivalent to this type in the JavaFX class hierarchy.

    /**
     * Creates a WidgetType for the given widget type.
     * @param name The script-level type name.
     */
    public WidgetType(String name) {
        super(name);

        method("get",           this::_get);
        method("getProperties", this::_getProperties);
        method("listenTo",      this::_listenTo);
        method("set",           this::_set);
        method("toString",      this::_toString);
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
    public void extendsProxy(ProxyType<? super W> superProxy) {
        super.extendsProxy(superProxy);
        if (superProxy instanceof WidgetType widgetType) {
            properties.putAll(widgetType.properties);
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
        PropertyGetter<W,P> getter,
        ArgumentConverter<P> converter
    ) {
        var keyword = new Keyword(keywordName);
        var def = new RWPropertyDef<W,P>(keyword, getter, converter);
        properties.put(keyword, def);
    }

    /**
     * Defines a read-only JavaFX property.
     * @param keywordName The identifying keyword's name.
     * @param getter The getter for the property object.
     * @param <P> The property's value type.
     */
    public <P> void fxReadOnly(
        String keywordName,
        ReadOnlyPropertyGetter<W,P> getter
    ) {
        var keyword = new Keyword(keywordName);
        var def = new ROPropertyDef<W,P>(keyword, getter);
        properties.put(keyword, def);
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method get
    // @args keyword
    // @result value
    // Gets the value of the property with the given *keyword*.
    private Object _get(W widget, Joe joe, Args args) {
        args.exactArity(1, "get(keyword)");
        return toDef(joe, args.next()).getValue(widget);
    }

    //**
    // @method getProperties
    // @result joe.Set
    // Returns a readonly `Set` of the object's property keywords.
    private Object _getProperties(W obj, Joe joe, Args args) {
        args.exactArity(0, "getProperties()");
        return joe.readonlySet(properties.keySet());
    }

    //**
    // @method listenTo
    // @args keyword, callable
    // @result Listener
    // Adds a listener *callable* to the property with the given *keyword*,
    // returning a [[Listener]]; use the [[Listener]]'s `cancel()` method
    // to stop listening to the property.
    //
    // The *callable* should take three arguments:
    //
    // - The property keyword
    // - The old value of the property
    // - The new value of the property
    //
    // The *callable* will be called when the property's value changes.
    private Object _listenTo(W obj, Joe joe, Args args) {
        args.exactArity(2, "listenTo(keyword, callable");
        var keyword = joe.toKeyword(args.next());
        var def =  toDef(joe, keyword);
        var prop =  def.getObservable(obj);
        var handler = args.next();

        var listener = new ObservableListener(prop, joe, keyword, handler);
        prop.addListener(listener);
        return listener;
    }

    //**
    // @method set
    // @args keyword, value
    // @result this
    // Sets the *value* of the property with the given *keyword*.
    // The *value* must be assignable to the property's value type.
    private Object _set(W obj, Joe joe, Args args) {
        args.exactArity(2, "set(keyword, value)");

        var def = toDef(joe, args.next());
        if (def instanceof WidgetType.RWPropertyDef<W,?> rw) {
            rw.setProperty(joe, obj, args.next());
        } else {
            throw new JoeError(
                "Property is read-only: '" + def.keyword() + "'");
        }
        return obj;
    }


    //**
    // @method toString
    // @result joe.String
    // Returns the value's string representation.
    private Object _toString(W obj, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return stringify(joe, obj);
    }

    //-------------------------------------------------------------------------
    // Utilities

    private PropertyDef<W,?> toDef(Joe joe, Object arg) {
        var def = properties.get(joe.toKeyword(arg));

        if (def == null) {
            throw joe.expected("property keyword", arg);
        }

        return def;
    }

    //-------------------------------------------------------------------------
    // Property Interfaces

    /**
     * A functional interface for retrieving a read/write JavaFX property
     * from a widget.
     * @param <W> The widget type
     * @param <P> The property type
     */
    public interface PropertyGetter<W, P> {
        /**
         * Gets the value of a widget property.
         * @param widget The widget
         * @return The property value
         */
        Property<P> get(W widget);
    }

    /**
     * A functional interface for retrieving a read/write JavaFX property
     * from a widget.
     * @param <W> The widget type
     * @param <P> The property type
     */
    public interface ReadOnlyPropertyGetter<W, P> {
        /**
         * Gets the value of a read-only widget property.
         * @param widget The widget
         * @return The property value
         */
        ReadOnlyProperty<P> get(W widget);
    }

    //-------------------------------------------------------------------------
    // Property Definitions

    // TODO Remove P?
    private sealed interface PropertyDef<W,P>
        permits RWPropertyDef, ROPropertyDef
    {
        Keyword keyword();
        Object getValue(W widget);
        ObservableValue<P> getObservable(W widget);
    }

    private record RWPropertyDef<W, P>(
        Keyword keyword,
        PropertyGetter<W,P> getter,
        ArgumentConverter<P> converter
    ) implements PropertyDef<W, P> {
        public Property<P> getObservable(W widget) {
            return getter.get(widget);
        }

        public Object getValue(W widget) {
            return getObservable(widget).getValue();
        }

        void setProperty(Joe joe, W obj, Object arg) {
            var property = getObservable(obj);

            try {
                property.setValue(converter.convert(joe, arg));
            } catch (Exception ex) {
                throw new JoeError(
                    "Failed to set property:" + ex.getMessage());
            }
        }
    }

    private record ROPropertyDef<W, P>(
        Keyword keyword,
        ReadOnlyPropertyGetter<W,P> getter
    ) implements PropertyDef<W, P> {
        public ReadOnlyProperty<P> getObservable(W obj) {
            return getter.get(obj);
        }

        public Object getValue(W widget) {
            return getObservable(widget).getValue();
        }
    }
}
