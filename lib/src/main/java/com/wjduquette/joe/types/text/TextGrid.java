package com.wjduquette.joe.types.text;

import java.util.Arrays;

/**
 * TextGrid is a tool for laying out blocks of text in a grid.
 */
public class TextGrid {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The number of columns and rows
    private int numCols = 0;
    private int numRows = 0;

    // The grid
    private StringBuilder[][] grid = new StringBuilder[0][0];

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
        numCols = Math.max(numCols, column + 1);
        numRows = Math.max(numRows, row + 1);

        if (grid.length < numRows) {
            grid = Arrays.copyOf(grid, numRows);
        }

        for (var i = 0; i < numRows; i++) {
            if (grid[i] == null) {
                grid[i] = new StringBuilder[numCols];
            } else if (grid[i].length < numCols) {
                grid[i] = Arrays.copyOf(grid[i], numCols);
            }
        }
    }

    //-------------------------------------------------------------------------
    // Formatting

    @Override
    public String toString() {
        // FIRST, return an empty string if there are no rows or columns
        if (numRows == 0) {
            return "";
        }

        // Check state
        checkInvariants();

        // FIRST, get the width of each column in characters and the
        // height of each row in lines.
        var widths = new int[numCols];
        var heights = new int[numRows];
        computeWidthsAndHeights(widths, heights);

        int gridWidth = Arrays.stream(widths).sum() +
            columnGap * (numCols - 1);

        var buff = new StringBuilder();
        for (var r = 0; r < numRows; r++) {
            formatRow(buff, r, widths, heights[r]);
            if (r < numRows - 1) {
                addRowGap(buff, gridWidth);
            }
        }

        return buff.toString().stripTrailing();
    }

    private void checkInvariants() {
        if (grid.length != numRows) {
            throw new IllegalStateException(
                "grid.length " + grid.length + " != numRows " + numRows);
        }
        for (var r = 0; r < numRows; r++) {
            if (grid[r].length != numCols) {
                throw new IllegalStateException(
                    "For row " + r + ",  row length " + grid[r].length +
                    " != numCols " + numCols);
            }
        }
    }

    private void computeWidthsAndHeights(int[] widths, int[] heights) {
        for (var r = 0; r < numRows; r++) {
            for (var c = 0; c < numCols; c++) {
                var text = cellText(r, c);
                var height = text.lines().count();
                var width = text.lines().mapToLong(String::length).max().orElse(0);

                widths[c] = Math.max(widths[c], (int)width);
                heights[r] = Math.max(heights[r], (int)height);
            }
        }
    }

    private void formatRow(StringBuilder buff, int r, int[] widths, int height) {
        // FIRST, get the individual lines.
        var blocks = new String[widths.length][height];
        for (var c = 0; c < numCols; c++) {
            var list = cellText(r, c).lines().toList();
            for (var k = 0; k < height; k++) {
                blocks[c][k] = k < list.size()
                    ? list.get(k)
                    : "";
            }
        }

        // One line of text
        for (var k = 0; k < height; k++) {
            for (var c = 0; c < numCols; c++) {
                buff.append(pad(blocks[c][k], widths[c]));

                if (c < numCols - 1 && columnGap > 0) {
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

    private String cellText(int r, int c) {
        var buff = grid[r][c];
        return buff != null ? buff.toString().stripTrailing() : "";
    }

    private String pad(String text, int width) {
        // Preserve leading whitespace when left-justified.
        text = text.stripTrailing();
        var pad = width - text.length();
        return text + " ".repeat(pad);
    }
}
