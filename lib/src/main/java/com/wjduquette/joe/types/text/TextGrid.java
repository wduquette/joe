package com.wjduquette.joe.types.text;

import java.util.Arrays;

/**
 * TextGrid is a tool for laying out blocks of text in a grid.
 */
public class TextGrid {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The grid
    private StringBuilder[][] grid = new StringBuilder[0][0];

    // Column alignment
    private TextAlign[] alignment = new TextAlign[0];

    // The spacing between columns in blank spaces
    private int columnGap = 0;

    // The spacing between rows, in blank lines
    private int rowGap = 0;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new TextGrid.
     */
    public TextGrid() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // API

    /**
     * Gets the gap between columns, in space characters.
     * @return The gap
     */
    @SuppressWarnings("unused")
    public int getColumnGap() {
        return columnGap;
    }

    /**
     * Sets the gap between columns, in space characters.
     * @param columnGap The gap
     */
    public void setColumnGap(int columnGap) {
        this.columnGap = columnGap;
    }

    /**
     * Gets the gap between rows, in blank lines.
     * @return The gap
     */
    @SuppressWarnings("unused")
    public int getRowGap() {
        return rowGap;
    }

    /**
     * Sets the gap between rows, in blank lines.
     * @param rowGap The gap
     */
    public void setRowGap(int rowGap) {
        this.rowGap = rowGap;
    }

    /**
     * Sets the alignment for the given column.  The default is
     * TextAlign.LEFT.
     * @param column The column index
     * @param align The alignment
     */
    public void setColumnAlignment(int column, TextAlign align) {
        extendTo(column, 0);
        alignment[column] = align;
    }

    /**
     * Puts the given text into the grid at the given row and column.
     * @param column The column
     * @param row The row
     * @param text The text
     * @return this
     */
    public TextGrid put(int column, int row, String text) {
        extendTo(column, row);

        grid[row][column] = new StringBuilder(text);
        return this;
    }

    // Ensures that grid is a rectangular array big enough to contain the
    // given row and column.
    private void extendTo(int column, int row) {
        if (grid.length < row + 1) {
            grid = Arrays.copyOf(grid, row + 1);
        }

        for (var i = 0; i < grid.length; i++) {
            if (grid[i] == null) {
                grid[i] = new StringBuilder[column + 1];
            } else if (grid[i].length < column + 1) {
                grid[i] = Arrays.copyOf(grid[i], column + 1);
            }
        }

        if (alignment.length < column + 1) {
            alignment = Arrays.copyOf(alignment, column + 1);
        }
    }

    //-------------------------------------------------------------------------
    // Formatting

    @Override
    public String toString() {
        // FIRST, return an empty string if there are no rows or columns
        if (grid.length == 0) {
            return "";
        }

        // FIRST, get the width of each column in characters and the
        // height of each row in lines.
        var widths = new int[grid[0].length];
        var heights = new int[grid.length];
        computeWidthsAndHeights(widths, heights);

        int gridWidth = Arrays.stream(widths).sum() +
            columnGap * (widths.length - 1);

        var buff = new StringBuilder();
        for (var i = 0; i < grid.length; i++) {
            formatRow(buff, i, widths, heights[i]);
            if (i < grid.length - 1) {
                addRowGap(buff, gridWidth);
            }
        }

        return buff.toString().stripTrailing();
    }

    private void computeWidthsAndHeights(int[] widths, int[] heights) {
        for (var i = 0; i < heights.length; i++) {
            for (var j = 0; j < widths.length; j++) {
                var text = cellText(i, j);
                var height = text.lines().count();
                var width = text.lines().mapToLong(String::length).max().orElse(0);

                widths[j] = Math.max(widths[j], (int)width);
                heights[i] = Math.max(heights[i], (int)height);
            }
        }
    }

    private void formatRow(StringBuilder buff, int i, int[] widths, int height) {
        // FIRST, get the individual lines.
        var blocks = new String[widths.length][height];
        for (var j = 0; j < widths.length; j++) {
            var list = cellText(i, j).lines().toList();
            for (var k = 0; k < height; k++) {
                blocks[j][k] = k < list.size()
                    ? list.get(k)
                    : "";
            }
        }

        // One line of text
        for (var k = 0; k < height; k++) {
            for (var j = 0; j < widths.length; j++) {
                buff.append(pad(blocks[j][k], alignment[j], widths[j]));

                if (j < widths.length - 1 && columnGap > 0) {
                    buff.append(" ".repeat(columnGap));
                }
            }
            buff.append("\n");
        }
    }

    private void addRowGap(StringBuilder buff, int gridWidth) {
        var line = " ".repeat(gridWidth);
        for (var i = 0; i < rowGap; i++) {
            buff.append(line).append("\n");
        }
    }

    private String cellText(int i, int j) {
        var buff = grid[i][j];
        return buff != null ? buff.toString().stripTrailing() : "";
    }

    private String pad(String text, TextAlign align, int width) {
        if (align == null) {
            align = TextAlign.LEFT;
        }

        return switch (align) {
            case LEFT -> {
                // Preserve leading whitespace when left-justified.
                text = text.stripTrailing();
                var pad = width - text.length();
                yield text + " ".repeat(pad);
            }
            case CENTER -> {
                text = text.strip();
                var pad = width - text.length();
                var p1 = pad / 2;
                var p2 = Math.max(pad - p1, 0);
                yield " ".repeat(p1) + text + " ".repeat(p2);
            }
            case RIGHT -> {
                text = text.strip();
                var pad = width - text.length();
                yield " ".repeat(pad) + text;
            }
        };
    }
}
