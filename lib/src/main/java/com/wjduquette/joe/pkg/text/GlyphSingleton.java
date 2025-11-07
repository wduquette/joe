package com.wjduquette.joe.pkg.text;

import com.wjduquette.joe.ProxyType;

/**
 * A singleton providing access to various Unicode symbols that I've found
 * to be useful.
 */
public class GlyphSingleton extends ProxyType<Void> {
    /** An instance of the type, for installation. */
    public static final GlyphSingleton TYPE = new GlyphSingleton();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the instance.
     */
    public GlyphSingleton() {
        super("Glyph");

        //**
        // @package joe.text
        // @type Glyph
        // The Glyph singleton defines constants for a variety of Unicode
        // glyphs that are useful in scripted output, especially with the
        // [[TextCanvas]].
        //
        // | Constant                             | Unicode  | Glyph |
        // | ------------------------------------ | -------- | ----- |
        // | `Glyph.BLACK_LEFT_POINTING_TRIANGLE` | `\u25C0` | `◀`   |
        // | `Glyph.LIGHT_DOWN_AND_HORIZONTAL`    | `\u252C` | `┬`   |
        // | `Glyph.LIGHT_HORIZONTAL`             | `\u2500` | `─`   |
        // | `Glyph.LIGHT_UP_AND_HORIZONTAL`      | `\u2534` | `┴`   |
        // | `Glyph.LIGHT_VERTICAL`               | `\u2502` | `│`   |
        // | `Glyph.LIGHT_VERTICAL_AND_LEFT`      | `\u2524` | `┤`   |
        // | `Glyph.WHITE_DOWN_POINTING_TRIANGLE` | `\u25BD` | `▽`   |
        // | `Glyph.WHITE_UP_POINTING_TRIANGLE`   | `\u25B3` | `△`   |
        staticType();

        constant("BLACK_LEFT_POINTING_TRIANGLE", "\u25C0");
        constant("LIGHT_DOWN_AND_HORIZONTAL", "\u252C");
        constant("LIGHT_HORIZONTAL", "\u2500");
        constant("LIGHT_UP_AND_HORIZONTAL", "\u2534");
        constant("LIGHT_VERTICAL", "\u2502");
        constant("LIGHT_VERTICAL_AND_LEFT", "\u2524");
        constant("WHITE_DOWN_POINTING_TRIANGLE", "\u25BD");
        constant("WHITE_UP_POINTING_TRIANGLE", "\u25B3");
    }
}
