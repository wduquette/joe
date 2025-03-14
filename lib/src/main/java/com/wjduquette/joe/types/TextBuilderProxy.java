package com.wjduquette.joe.types;

import com.wjduquette.joe.*;

/**
 * A ProxyType for the TextBuilder type.
 */
public class TextBuilderProxy extends ProxyType<TextBuilder> {
    /**
     * The proxy's TYPE constant.
     */
    public static final TextBuilderProxy TYPE = new TextBuilderProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe
    // @type TextBuilder
    // `TextBuilder` is a native type similar to a Java `StringBuilder`; it's
    // used for building up text strings a little at a time.
    //
    // Joe classes can extend the `TextBuilder` type.

    /**
     * Creates the proxy.
     */
    public TextBuilderProxy() {
        super("TextBuilder");
        proxies(TextBuilder.class);
        initializer(this::_init);

        method("append",   this::_append);
        method("clear",    this::_clear);
        method("print",    this::_print);
        method("printf",   this::_printf);
        method("println",  this::_println);
        method("toString", this::_toString);
    }

    //-------------------------------------------------------------------------
    // JoeClass API

    @Override
    public boolean canBeExtended() {
        return true;
    }

    @Override
    public JoeValue make(Joe joe, JoeClass joeClass) {
        return new TextBuilder(joeClass);
    }

    //-------------------------------------------------------------------------
    // Initializer Implementation

    //**
    // @init
    // Creates an empty `TextBuilder`.
    private Object _init(Joe joe, Args args) {
        args.exactArity(0, "TextBuilder()");
        return make(null, this);
    }

    //-------------------------------------------------------------------------
    // Method Implementation

    //**
    // @method append
    // @args value
    // @result this
    // Adds the value to the buffer.
    private Object _append(TextBuilder buff, Joe joe, Args args) {
        args.exactArity(1, "append(value)");
        buff.append(joe.stringify(args.next()));
        return buff;
    }

    //**
    // @method clear
    // @result this
    // Clears the buffer.
    private Object _clear(TextBuilder buff, Joe joe, Args args) {
        args.exactArity(0, "clear()");
        buff.clear();
        return buff;
    }

    //**
    // @method print
    // @args value
    // @result this
    // Adds the value to the buffer.
    private Object _print(TextBuilder buff, Joe joe, Args args) {
        args.exactArity(1, "print(value)");
        buff.append(joe.stringify(args.next()));
        return buff;
    }

    //**
    // @method printf
    // @args fmt, [values...]
    // Formats its arguments given the *fmt* string, and appends the result
    // to the buffer.  See [[String#topic.formatting]] for the format
    // string syntax.
    private Object _printf(TextBuilder buff, Joe joe, Args args) {
        args.minArity(1, "printf(fmt, [values]...)");
        var fmt = joe.toString(args.next());

        buff.append(StringFormatter.format(joe, fmt, args.remainderAsList()));
        return buff;
    }

    //**
    // @method println
    // @args value
    // @result this
    // Adds the value to the buffer, followed by a new line.
    private Object _println(TextBuilder buff, Joe joe, Args args) {
        args.exactArity(1, "println(value)");
        buff.append(joe.stringify(args.next())).append("\n");
        return buff;
    }

    //**
    // @method toString
    // @result String
    // Returns the string.
    private Object _toString(TextBuilder buff, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return buff.toString();
    }
}
