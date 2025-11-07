package com.wjduquette.joe.pkg.text;

import com.wjduquette.joe.NativePackage;

/**
 * The joe.text package object.
 */
public class JoeTextPackage extends NativePackage {
    /** The package, for registration and installation. */
    public static final JoeTextPackage PACKAGE = new JoeTextPackage();

    /**
     * Creates an instance of the package.
     */
    public JoeTextPackage() {
        super("joe.text");

        //**
        // @package joe.text
        // %title Text Utilities
        // The `joe.text` package contains APIs for advanced text
        // formatting, and particularly for the formatting of
        // monospace text for display in the terminal.
        //
        // The `joe.text` package is not included in vanilla Joe interpreters,
        // but is available for import in `joe run`, `joe test`, and so on.

        type(GlyphSingleton.TYPE);
        type(TextBuilderClass.TYPE);
        type(TextCanvasClass.TYPE);
    }
}
