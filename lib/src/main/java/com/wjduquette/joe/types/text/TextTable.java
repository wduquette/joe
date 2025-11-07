package com.wjduquette.joe.types.text;

import java.util.ArrayList;
import java.util.List;

/**
 * A TextTable is a template for outputting a tabular layout related to a list
 * of records.  The client defines the column headers, alignments, and so forth,
 * and the TextTable formats text output.
 * @param <R> The record type
 */
@SuppressWarnings("unused")
public class TextTable<R> {
    private static final String LIGHT_HORIZONTAL = "\u2500";
    private static final String LIGHT_VERTICAL = "\u2502";
    private static final String LIGHT_VERTICAL_AND_HORIZONTAL = "\u253C";

    /** Output mode. */
    public enum Mode {
        /** Outputs a Markdown table */
        MARKDOWN,

        /** Outputs a table using Unicode box drawing characters. */
        TERMINAL
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    private final List<TextColumn<R,?>> columns = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new text table.
     */
    public TextTable() {
    }

    /**
     * Creates a new text table given the column definitions.
     * @param columns The columns.
     */
    public TextTable(List<TextColumn<R,?>> columns) {
        this.columns.addAll(columns);
    }

    //-------------------------------------------------------------------------
    // Public Methods

    /**
     * Gets the list of columns.
     * @return The list
     */
    public List<TextColumn<R, ?>> getColumns() {
        return columns;
    }

    /**
     * Returns a table given the rows of data and the column definitions,
     * formatted according to the given mode.
     * @param rows The rows of data.
     * @param mode The mode
     * @return The text output
     */
    public String toTable(List<R> rows, Mode mode) {
        return new Formatter(mode, rows).toString();
    }

    /**
     * Returns a Markdown table given the rows of data and the column
     * definitions.
     * given mode.
     * @param rows The rows of data.
     * @return The Markdown output
     */
    public String toMarkdown(List<R> rows) {
        return new Formatter(Mode.MARKDOWN, rows).toString();
    }

    /**
     * Returns a table formatted for output to the terminal using
     * Unicode characters, given the rows of data and the column
     * definitions.
     * @param rows The rows of data.
     * @return The Markdown output
     */
    public String toTerminal(List<R> rows) {
        return new Formatter(Mode.TERMINAL, rows).toString();
    }

    private class Formatter {
        private final Mode mode;
        private final List<R> rows;
        private final int[] widths;
        private final StringBuilder buff = new StringBuilder();

        Formatter(Mode mode, List<R> rows) {
            this.mode = mode;
            this.rows = rows;
            this.widths = getColumnWidths(rows);
        }

        private int[] getColumnWidths(List<R> rows) {
            var widths = new int[columns.size()];

            for (int c = 0; c < columns.size(); c++) {
                var column = columns.get(c);
                widths[c] = Math.max(3, column.getHeader().length());

                for (var row : rows) {
                    var value = column.getValueGetter().apply(row).toString();
                    widths[c] = Math.max(widths[c], value.length());
                }
            }

            return widths;
        }

        public String toString() {
            layoutHeader();
            layoutSeparator();

            for (var row : rows) {
                layoutRow(row);
            }

            return buff.toString();
        }

        private void layoutHeader() {
            if (mode == Mode.MARKDOWN) {
                buff.append(vLine(mode)).append(" ");
            }

            for (var c = 0; c < columns.size(); c++) {
                pad(c, columns.get(c).getHeader());

                if (c < columns.size() - 1) {
                    buff.append(" ").append(vLine(mode)).append(" ");
                }
            }

            if (mode == Mode.MARKDOWN) {
                buff.append(" ").append(vLine(mode));
            }
            buff.append("\n");
        }

        private void layoutSeparator() {
            if (mode == Mode.MARKDOWN) {
                buff.append(vLine(mode)).append(" ");
            }

            for (var c = 0; c < columns.size(); c++) {
                if (mode == Mode.MARKDOWN) {
                    var wid = widths[c];
                    var sep = switch (columns.get(c).getAlignment()) {
                        case LEFT -> "-".repeat(wid);
                        case CENTER -> ":" + "-".repeat(wid - 2) + ":";
                        case RIGHT -> "-".repeat(wid - 1) + ":";
                    };
                    buff.append(sep);
                } else {
                    buff.append(hLine(mode).repeat(widths[c]));
                }

                if (c < columns.size() - 1) {
                    if (mode == Mode.MARKDOWN) {
                        buff.append(" ")
                            .append(cross(mode))
                            .append(" ");
                    } else {
                        buff.append(hLine(mode))
                            .append(cross(mode))
                            .append(hLine(mode));
                    }
                }
            }

            if (mode == Mode.MARKDOWN) {
                buff.append(" ").append(vLine(mode));
            }
            buff.append("\n");
        }

        private void layoutRow(R row) {
            if (mode == Mode.MARKDOWN) {
                buff.append(vLine(mode)).append(" ");
            }

            for (var c = 0; c < columns.size(); c++) {
                pad(c, columns.get(c).getValueGetter()
                    .apply(row).toString());

                if (c < columns.size() - 1) {
                    buff.append(" ").append(vLine(mode)).append(" ");
                }
            }

            if (mode == Mode.MARKDOWN) {
                buff.append(" ").append(vLine(mode));
            }
            buff.append("\n");

        }

        private void pad(int c, String value) {
            var width = widths[c];
            var delta = width - value.length();
            var left = delta/2;
            var right = delta - left;

            var padded = switch (columns.get(c).getAlignment()) {
                case LEFT -> value + " ".repeat(delta);
                case CENTER -> " ".repeat(left) + value + " ".repeat(right);
                case RIGHT -> " ".repeat(delta) + value;
            };

            buff.append(padded);
        }

        private String vLine(Mode mode) {
            return (mode == Mode.MARKDOWN) ? "|" : LIGHT_VERTICAL;
        }

        private String hLine(Mode mode) {
            return (mode == Mode.MARKDOWN) ? "-" : LIGHT_HORIZONTAL;
        }

        private String cross(Mode mode) {
            return (mode == Mode.MARKDOWN) ? "|" : LIGHT_VERTICAL_AND_HORIZONTAL;
        }
    }

}
