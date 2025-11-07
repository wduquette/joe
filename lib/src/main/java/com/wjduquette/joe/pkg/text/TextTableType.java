package com.wjduquette.joe.pkg.text;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.ProxyType;

/**
 * The TextTable binding.
 */
public class TextTableType extends ProxyType<TextTableValue> {
    /** The type, for installation. */
    public static final TextTableType TYPE = new TextTableType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.text
    // @type TextTable
    // A [[TextTable]] is used to format a list of records as a text table
    // given a list of [[TextColumn]] definitions.
    /** Constructor */
    public TextTableType() {
        super("TextTable");
        proxies(TextTableValue.class);

        initializer(this::_init);

        method("column",     this::_column);
        method("columns",    this::_columns);
        method("toMarkdown", this::_toMarkdown);
        method("toTerminal", this::_toTerminal);
    }

    //-------------------------------------------------------------------------
    // Stringifier

    @Override
    public String stringify(Joe joe, Object value) {
        assert value instanceof TextTableValue;
        var tc = (TextTableValue)value;
        return "TextTable[" + tc.getColumns() + "]";
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Creates a new TextTable.
    private Object _init(Joe joe, Args args) {
        args.exactArity(0, "TextTable()");
        return new TextTableValue();
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method column
    // %args column
    // %result this
    // Adds a [[TextColumn]] to the table.
    private Object _column(TextTableValue tc, Joe joe, Args args) {
        args.exactArity(1, "column(column)");
        tc.getColumns().add(joe.toType(TextColumnValue.class, args.next()));
        return tc;
    }

    //**
    // @method columns
    // %result joe.List
    // Returns the list of [[TextColumn|TextColumns]], which can
    // be updated freely.
    private Object _columns(TextTableValue tc, Joe joe, Args args) {
        args.exactArity(0, "columns()");
        return joe.wrapList(tc.getColumns(), TextColumnValue.class);
    }

    //**
    // @method toMarkdown
    // %args values
    // %result joe.String
    // Given a [[joe.List]] of *values* and the column definitions, returns
    // a table in Markdown format.
    private Object _toMarkdown(TextTableValue tc, Joe joe, Args args) {
        args.exactArity(1, "toMarkdown(values)");
        return tc.toMarkdown(joe.toList(args.next()));
    }

    //**
    // @method toTerminal
    // %args values
    // %result joe.String
    // Given a [[joe.List]] of *values* and the column definitions, returns
    // a table suitable for output to a terminal.
    private Object _toTerminal(TextTableValue tc, Joe joe, Args args) {
        args.exactArity(1, "toTerminal(values)");
        return tc.toTerminal(joe.toList(args.next()));
    }
}
