package com.wjduquette.joe.types.text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A canvas for plotting characters on an X/Y plane, suitable for output
 * to a terminal or inclusion in source code.  Characters cells are
 * counted from (0,0), which is the upper left cell.  The canvas will
 * expand as needed.
 */
@SuppressWarnings("unused")
public class TextCanvas {
    /** Space character. */
    public static final char BLANK = ' ';

    // From Unicode Box Drawing, Block Elements, Geometric Figures, 2500-25FF
    /** Unicode character */ public static final String LIGHT_HORIZONTAL = "\u2500";
    /** Unicode character */ public static final String LIGHT_VERTICAL = "\u2502";
    /** Unicode character */ public static final String LIGHT_DOWN_AND_HORIZONTAL = "\u252C";
    /** Unicode character */ public static final String LIGHT_UP_AND_HORIZONTAL = "\u2534";
    /** Unicode character */ public static final String LIGHT_VERTICAL_AND_LEFT = "\u2524";
    /** Unicode character */ public static final String WHITE_UP_POINTING_TRIANGLE = "\u25B3";
    /** Unicode character */ public static final String WHITE_DOWN_POINTING_TRIANGLE = "\u25BD";
    /** Unicode character */ public static final String BLACK_LEFT_POINTING_TRIANGLE = "\u25C0";

    //-------------------------------------------------------------------------
    // Instance Variables

    private final List<Row> rows = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new TextCanvas.
     */
    public TextCanvas() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // Accessors

    /**
     * Gets a character from the canvas as a string.
     * @param c The column
     * @param r The row
     * @return The string
     */
    public String get(int c, int r) {
        if (r >= rows.size()) return "" + BLANK;
        return "" + rows.get(r).get(c);
    }

    /**
     * Puts a text string into the canvas, horizontally, starting at the
     * given cell.
     * @param c The column
     * @param r The row
     * @param text the text
     */
    public void put(int c, int r, String text) {
        extendRows(r);
        for (int i = 0; i < text.length(); i++) {
            putChar(c + i, r, text.charAt(i));
        }
    }

    /**
     * Fills a canvas region with the given character.
     * @param ch The character
     * @param c The left column of the region
     * @param r The top row of the region
     * @param width The region's width in columns
     * @param height The region's height in rows
     */
    public void fill(char ch, int c, int r, int width, int height) {
        extendRows(r + height - 1);
        for (var i = 0; i < height; i++) {
            for (var j = 0; j < width; j++) {
                putChar(c + j, r + i, ch);
            }
        }
    }


    /**
     * Puts a text string into the canvas, horizontally, extending left
     * from the given cell, so that the final character of the string
     * is at the given coordinates.  Text extending left of column 0 is
     * clipped.
     * @param c The column
     * @param r The row
     * @param text the text
     */
    public void putLeft(int c, int r, String text) {
        var c1 = c - text.length() + 1;
        if (c1 < 0) {
            var delta = -c1;
            c1 = 0;
            text = text.substring(delta);
        }
        put(c1, r, text);
    }

    /**
     * Puts a text string into the canvas, vertically, extending down
     * from the given cell.
     * @param c The column
     * @param r The row
     * @param text the text
     */
    public void putDown(int c, int r, String text) {
        extendRows(r + text.length() - 1);
        for (int i = 0; i < text.length(); i++) {
            putChar(c, r + i, text.charAt(i));
        }
    }

    /**
     * Puts a text string into the canvas, vertically, extending up
     * from the given cell, so that the final character of the string
     * is at the given coordinates.  Text extending above row 0 is
     * clipped.
     * @param c The column
     * @param r The row
     * @param text the text
     */
    public void putUp(int c, int r, String text) {
        var r1 = r - text.length() + 1;
        if (r1 < 0) {
            var delta = -r1;
            r1 = 0;
            text = text.substring(delta);
        }
        putDown(c, r1, text);
    }

    // Puts a character into the canvas at the given location.
    // extendRows(r) should already have been called.
    private void putChar(int c, int r, char ch) {
        rows.get(r).put(c, ch);
    }

    /**
     * Gets the width of the canvas in character columns, based on
     * its content.
     * @return The width
     */
    public int getWidth() {
        return rows.stream()
            .mapToInt(row -> row.data.size())
            .max().orElse(0);
    }

    /**
     * Gets the height of the canvas in lines of text columns, based on
     * its content.
     * @return The width
     */
    public int getHeight() {
        return rows.size();
    }

    /**
     * Returns the content of the canvas as a string.
     * @return The string
     */
    public String toString() {
        var width = rows.stream()
            .mapToInt(Row::length)
            .max()
            .orElse(0);
        return rows.stream()
            .map(r -> r.paddedTo(width))
            .collect(Collectors.joining("\n"));
    }

    private void extendRows(int r) {
        while (rows.size() < r + 1) {
            rows.add(new Row());
        }
    }

    //-------------------------------------------------------------------------
    // Helper Types

    private static class Row {
        private final List<Character> data = new ArrayList<>();

        public void put(int c, char ch) {
            extendData(c);
            data.set(c, ch);
        }
        public char get(int c) {
            if (c >= data.size()) return BLANK;
            return data.get(c);
        }

        private void extendData(int c) {
            while (data.size() < c + 1) {
                data.add(BLANK);
            }
        }

        public int length() {
            return data.size();
        }

        public String paddedTo(int width) {
            var text = toString();
            if (text.length() < width) {
                text = text + " ".repeat(width - text.length());
            }
            return text;
        }

        public String toString() {
            return data.stream()
                .map(ch -> Character.toString(ch))
                .collect(Collectors.joining());
        }
    }
}
