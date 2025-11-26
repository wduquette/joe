package com.wjduquette.joe.pkg.text;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.ProxyType;
import com.wjduquette.joe.types.text.TextAlign;
import com.wjduquette.joe.types.text.TextGrid;

/**
 * The TextGrid binding.
 */
public class TextGridType extends ProxyType<TextGrid> {
    /** The type, for installation. */
    public static final TextGridType TYPE = new TextGridType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.text
    // @type TextGrid
    // A [[TextGrid]] is used to format a grid of blocks of text.
    /** Constructor */
    public TextGridType() {
        super("TextGrid");
        proxies(TextGrid.class);

        initializer(this::_init);


        method("put",              this::_put);
        method("columnAlignment",  this::_columnAlignment);
        method("columnGap",        this::_columnGap);
        method("rowGap",           this::_rowGap);
        method("toString",         this::_toString);
    }

    //-------------------------------------------------------------------------
    // Stringifier

    @Override
    public String stringify(Joe joe, Object value) {
        assert value instanceof TextGrid;
        var grid = (TextGrid)value;
        return grid.toString();
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Creates a new TextGrid.
    private Object _init(Joe joe, Args args) {
        args.exactArity(0, "TextGrid()");
        return new TextGrid();
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method put
    // %args column, row, text
    // %result this
    // Puts the *text* into the grid cell, replacing any previous content.
    private Object _put(TextGrid grid, Joe joe, Args args) {
        args.exactArity(3, "put(column, row, text");
        var c = joe.toCount(args.next());
        var r = joe.toCount(args.next());
        var s = joe.stringify(args.next());
        grid.put(c, r, s);
        return grid;
    }

    //**
    // @method columnAlignment
    // %args column, align
    // %result this
    // Sets the *column*'s [[TextAlign]].  The default is left.
    private Object _columnAlignment(TextGrid grid, Joe joe, Args args) {
        args.exactArity(2, "columnAlignment(column, align)");
        var column = joe.toCount(args.next());
        var align = joe.toEnum(args.next(), TextAlign.class);
        grid.setColumnAlignment(column, align);
        return grid;
    }

    //**
    // @method columnGap
    // %args gap
    // %result this
    // Sets the column gap in characters.  The default is 0.
    private Object _columnGap(TextGrid grid, Joe joe, Args args) {
        args.exactArity(1, "columnGap(gap)");
        grid.setColumnGap(joe.toCount(args.next()));
        return grid;
    }

    //**
    // @method rowGap
    // %args gap
    // %result this
    // Sets the row gap in characters.  The default is 0.
    private Object _rowGap(TextGrid grid, Joe joe, Args args) {
        args.exactArity(1, "rowGap(gap)");
        grid.setRowGap(joe.toCount(args.next()));
        return grid;
    }

    //**
    // @method toString
    // %result joe.String
    // Returns the formatted grid.
    private Object _toString(TextGrid grid, Joe joe, Args args) {
        args.exactArity(0, "toString");
        return grid.toString();
    }

}
