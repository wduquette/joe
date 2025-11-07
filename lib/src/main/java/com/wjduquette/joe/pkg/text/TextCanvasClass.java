package com.wjduquette.joe.pkg.text;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeClass;
import com.wjduquette.joe.ProxyType;
import com.wjduquette.joe.types.ListValue;
import com.wjduquette.joe.types.text.TextCanvas;

/**
 * Binding for the TextCanvas class.
 */
public class TextCanvasClass extends ProxyType<TextCanvas> {
    /** The type, for installation. */
    public static final TextCanvasClass TYPE = new TextCanvasClass();

    //-------------------------------------------------------------------------
    // Constructor

    /** Creates the type. */
    public TextCanvasClass() {
        super("TextCanvas");
        proxies(TextCanvas.class);

        //**
        // @package joe.text
        // @type TextCanvas
        // The `TextCanvas` is a canvas for drawing diagrams using monospaced
        // text for output to the terminal.  It can be thought of as a
        // two-dimensional array of character cells, with (0,0) at the
        // top-left with columns extending to the left and rows extending
        // down.  Characters and strings can be inserted at any
        // (*column*, *row*) cell; the canvas will expand automatically.
        initializer(this::_init);

        method("asText",   this::_asText);
        method("fill",     this::_fill);
        method("get",      this::_get);
        method("height",   this::_height);
        method("put",      this::_put);
        method("putDown",  this::_putDown);
        method("putLeft",  this::_putLeft);
        method("putUp",    this::_putUp);
        method("size",     this::_size);
        method("width",    this::_width);
        method("toString", this::_toString);
    }

    //-------------------------------------------------------------------------
    // Stringify

    @Override
    public String stringify(Joe joe, Object value) {
        assert value instanceof TextCanvas;
        var tc = (TextCanvas)value;
        return "TextCanvas[" + tc.getWidth() + "x" + tc.getHeight() + "]";
    }

    //-------------------------------------------------------------------------
    // JoeClass API

    @Override
    public boolean canBeExtended() {
        return true;
    }

    @Override
    public Object make(Joe joe, JoeClass joeClass) {
        return new TextCanvasInstance(joeClass);
    }


    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Creates a new, empty TextCanvas of size [0, 0].
    private Object _init(Joe joe, Args args) {
        args.exactArity(0, "TextCanvas()");
        return make(joe, this);
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method asText
    // %result String
    // Returns the contents of the canvas as a String.
    private Object _asText(TextCanvas tc, Joe joe, Args args) {
        args.exactArity(0, "asText()");
        return tc.toString();
    }

    //**
    // @method fill
    // %args char, column, row, width, height
    // %result this
    // Fills the character cells in the given region with the given
    // character.
    private Object _fill(TextCanvas tc, Joe joe, Args args) {
        args.exactArity(5, "fill(char, column, row, width, height)");
        var text = joe.stringify(args.next());
        if (text.length() != 1) {
            throw joe.expected("character", text);
        }
        var ch = text.charAt(0);
        var c = toCellIndex(joe, args.next());
        var r = toCellIndex(joe, args.next());
        var w = toDimension(joe, args.next());
        var h = toDimension(joe, args.next());

        tc.fill(ch, c, r, w, h);
        return tc;
    }

    //**
    // @method get
    // %args column, row
    // %result String
    // Gets the character at (*column*, *row*) as a String
    private Object _get(TextCanvas tc, Joe joe, Args args) {
        args.exactArity(2, "get(column, row)");
        var c = toCellIndex(joe, args.next());
        var r = toCellIndex(joe, args.next());

        return tc.get(c, r);
    }

    //**
    // @method height
    // %result Number
    // Returns the height of the canvas in rows.
    private Object _height(TextCanvas tc, Joe joe, Args args) {
        args.exactArity(0, "height()");
        return (double)tc.getHeight();
    }

    //**
    // @method put
    // %args column, row, text
    // %result this
    // Writes the *text* to the canvas starting at (*column*, *row*)
    // and extending to the right. No provision is made for multiline
    // text.
    private Object _put(TextCanvas tc, Joe joe, Args args) {
        args.exactArity(3, "put(c, r, text)");
        var c = toCellIndex(joe, args.next());
        var r = toCellIndex(joe, args.next());
        var text = joe.stringify(args.next());

        tc.put(c, r, text);
        return tc;
    }

    //**
    // @method putLeft
    // %args column, row, text
    // %result this
    // Writes the *text* to the canvas starting at (*column*, *row*), and
    // extending to the left, i.e., the text's rightmost character will
    // be at (*column*, *row*).  Text that extends to the left of column
    // 0 is clipped.
    private Object _putLeft(TextCanvas tc, Joe joe, Args args) {
        args.exactArity(3, "putLeft(c, r, text)");
        var c = toCellIndex(joe, args.next());
        var r = toCellIndex(joe, args.next());
        var text = joe.stringify(args.next());

        tc.putLeft(c, r, text);
        return tc;
    }

    //**
    // @method putDown
    // %args column, row, text
    // %result this
    // Writes the *text* to the canvas starting at (*column*, *row*), and
    // extending down.
    private Object _putDown(TextCanvas tc, Joe joe, Args args) {
        args.exactArity(3, "putDown(c, r, text)");
        var c = toCellIndex(joe, args.next());
        var r = toCellIndex(joe, args.next());
        var text = joe.stringify(args.next());

        tc.putDown(c, r, text);
        return tc;
    }

    //**
    // @method putUp
    // %args column, row, text
    // %result this
    // Writes the *text* to the canvas starting at (*column*, *row*), and
    // extending up, i.e., the text's rightmost character will
    // be at (*column*, *row*).  Text that extends above row 0 is clipped.
    private Object _putUp(TextCanvas tc, Joe joe, Args args) {
        args.exactArity(3, "putUp(c, r, text)");
        var c = toCellIndex(joe, args.next());
        var r = toCellIndex(joe, args.next());
        var text = joe.stringify(args.next());

        tc.putUp(c, r, text);
        return tc;
    }

    //**
    // @method size
    // %result List
    // Returns the size of the canvas as a two-item list, \[*width*, *height*].
    private Object _size(TextCanvas tc, Joe joe, Args args) {
        args.exactArity(0, "size()");
        return ListValue.pair((double)tc.getWidth(), (double)tc.getHeight());
    }

    //**
    // @method width
    // %result Number
    // Returns the width of the canvas in columns.
    private Object _width(TextCanvas tc, Joe joe, Args args) {
        args.exactArity(0, "width()");
        return (double)tc.getWidth();
    }

    //**
    // @method toString
    // %result String
    // Returns the canvas's string representation, which is not its
    // content.
    private Object _toString(TextCanvas tc, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return stringify(joe, tc);
    }

    private int toCellIndex(Joe joe, Object arg) {
        var num = joe.toInteger(arg);
        if (num < 0) {
            throw joe.expected("non-negative number", arg);
        }
        return num;
    }

    private int toDimension(Joe joe, Object arg) {
        var num = joe.toInteger(arg);
        if (num <= 0) {
            throw joe.expected("positive number", arg);
        }
        return num;
    }
}
