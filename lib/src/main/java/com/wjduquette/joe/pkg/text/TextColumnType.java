package com.wjduquette.joe.pkg.text;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.ProxyType;
import com.wjduquette.joe.types.text.TextAlign;

/**
 * The TextColumn binding.
 */
public class TextColumnType extends ProxyType<TextColumnValue> {
    /** The type, for installation. */
    public static final TextColumnType TYPE = new TextColumnType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.text
    // @type TextColumn
    // A column in a [[TextTable]].
    /** Constructor */
    public TextColumnType() {
        super("TextColumn");
        proxies(TextColumnValue.class);

        initializer(this::_init);

        method("alignment",     this::_alignment);
        method("getAlignment",  this::_getAlignment);
        method("getGetter",     this::_getGetter);
        method("getHeader",     this::_getHeader);
        method("getter",        this::_getter);
        method("header",        this::_header);
    }

    //-------------------------------------------------------------------------
    // Stringifier

    @Override
    public String stringify(Joe joe, Object value) {
        assert value instanceof TextColumnValue;
        var tc = (TextColumnValue)value;
        return "TextColumn[" + tc.getHeader() + "," + tc.getAlignment() + "]";
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Creates a new TextColumn.
    private Object _init(Joe joe, Args args) {
        args.exactArity(0, "TextColumn()");
        return new TextColumnValue();
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method alignment
    // %args alignment
    // %result this
    // Sets the column's alignment.
    private Object _alignment(TextColumnValue tc, Joe joe, Args args) {
        args.exactArity(1, "alignment(alignment)");
        tc.setAlignment(joe.toEnum(args.next(), TextAlign.class));
        return tc;
    }

    //**
    // @method getAlignment
    // %result [[TextAlign]]
    // Gets the column's alignment.
    private Object _getAlignment(TextColumnValue tc, Joe joe, Args args) {
        args.exactArity(0, "getAlignment()");
        return tc.getAlignment();
    }

    //**
    // @method getGetter
    // %result callable
    // Gets the *callable/1* used to retrieve the column's value
    // from an input record.
    private Object _getGetter(TextColumnValue tc, Joe joe, Args args) {
        args.exactArity(0, "getGetter()");
        return Joe.unwrapCallable(tc.getValueGetter());
    }

    //**
    // @method getHeader
    // %result joe.String
    // Gets the column's header text
    private Object _getHeader(TextColumnValue tc, Joe joe, Args args) {
        args.exactArity(0, "getHeader()");
        return tc.getHeader();
    }

    //**
    // @method getter
    // %args callable
    // %result this
    // Provides a *callable/1* to retrieve the column's value
    // from an input record.
    private Object _getter(TextColumnValue tc, Joe joe, Args args) {
        args.exactArity(1, "getter(callable)");

        // Wrap using StringFunctionWrapper, so that the result is
        // always stringified.  TextTable itself calls toString(),
        // which will usually be wrong.
        tc.setValueGetter(joe.wrapStringFunction(args.next()));
        return tc;
    }

    //**
    // @method header
    // %args text
    // %result this
    // Sets the column's header text.
    private Object _header(TextColumnValue tc, Joe joe, Args args) {
        args.exactArity(1, "header(text)");
        tc.setHeader(joe.toString(args.next()));
        return tc;
    }
}
