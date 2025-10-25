package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.scene.control.Labeled;

class LabeledType extends WidgetType<Labeled> {
    public static final LabeledType TYPE = new LabeledType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @widget Labeled
    // %extends Control
    // The `Labeled` type is the base class for JavaFX widgets that display
    // text and an optional graphic, e.g., [[Label]] and [[Button]].
    public LabeledType() {
        super("Labeled");
        extendsProxy(ControlType.TYPE);
        proxies(Labeled.class);

        //**
        // @property alignment Pos
        // Alignment of content within the `Labeled`, when there is
        // empty space.
        fxProperty("text", Labeled::textProperty, Joe::toString);

        //**
        // @property contentDisplay ContentDisplay
        // Positioning of `#graphic` relative to `#text`.
        fxProperty("contentDisplay", Labeled::contentDisplayProperty, Win::toContentDisplay);

        //**
        // @property ellipsisString joe.String
        // Ellipsis string when text is truncated, e.g., "...".
        fxProperty("ellipsisString", Labeled::ellipsisStringProperty, Joe::toString);

        //**
        // @property graphic Node
        // Graphic to display with text.
        fxProperty("graphic", Labeled::graphicProperty, Win::toNode);

        //**
        // @property graphicTextGap joe.Number
        // Space between graphic and text, in pixels.
        fxProperty("graphicTextGap", Labeled::graphicTextGapProperty, Joe::toDouble);

        //**
        // @property lineSpacing joe.Number
        // Space between lines in pixels.
        fxProperty("lineSpacing", Labeled::lineSpacingProperty, Joe::toDouble);

        //**
        // @property mnemonicParsing joe.Boolean
        // Enable/disable text mnemonic parsing.
        fxProperty("mnemonicParsing", Labeled::mnemonicParsingProperty, Joe::toBoolean);

        //**
        // @property textAlignment TextAlignment
        // Alignment of multiline text.
        fxProperty("textAlignment", Labeled::textAlignmentProperty, Win::toTextAlignment);

        //**
        // @property text joe.String
        // The text to display
        fxProperty("text", Labeled::textProperty, Joe::toString);

        //**
        // @property underline joe.Boolean
        // Enable/disable underlining
        fxProperty("underline", Labeled::underlineProperty, Joe::toBoolean);

        //**
        // @property wrapText joe.Boolean
        // Wrap long strings onto multiple lines.
        fxProperty("wrapText", Labeled::wrapTextProperty, Joe::toBoolean);

        // Methods
        method("graphic",  this::_graphic);
        method("text",     this::_text);
        method("wrapText", this::_wrapText);
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method graphic
    // %args node
    // %result this
    // Sets the labeled's `#graphic` to the given [[Node]].
    private Object _graphic(Labeled node, Joe joe, Args args) {
        args.exactArity(1, "graphic(node)");
        node.setGraphic(Win.toNode(joe, args.next()));
        return node;
    }

    //**
    // @method text
    // %args text
    // %result this
    // Sets the labeled's `#text`.
    private Object _text(Labeled node, Joe joe, Args args) {
        args.exactArity(1, "text(text)");
        node.setText(joe.stringify(args.next()));
        return node;
    }

    //**
    // @method wrapText
    // %args flag
    // %result this
    // Sets the labeled's `#wrapText` flag.
    private Object _wrapText(Labeled node, Joe joe, Args args) {
        args.exactArity(1, "wrapText(flag)");
        node.setWrapText(joe.toBoolean(args.next()));
        return node;
    }
}
