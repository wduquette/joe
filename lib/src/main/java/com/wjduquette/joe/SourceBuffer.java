package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.List;

public class SourceBuffer {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The buffer's ID; this is usually the filename of a script file.
    private final String filename;

    // A script's source string
    private final String source;

    // The character index in source of the first character in each line.
    // lines.get(0) returns the start of Line 1.
    private final List<Integer> lines = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    public SourceBuffer(String filename, String source) {
        this.filename = filename;
        this.source = source;

        // FIRST, scan for lines.
        var len = source.length();
        lines.add(0);
        var newLine = false;
        for (int i = 0; i < len; i++) {
            if (newLine) {
                lines.add(i);
            }
            newLine = source.charAt(i) == '\n';
        }
    }

    //-------------------------------------------------------------------------
    // Methods

    public String filename() {
        return filename;
    }

    public String source() {
        return source;
    }

    public int lineCount() {
        return lines.size();
    }

    /**
     * Gets the text of the line with the given line number.  Strips any
     * trailing whitespace.
     * @param lineNumber The line number, starting at 1.
     * @return The text.
     */
    public String line(int lineNumber) {
        var index = lineNumber - 1;
        var start = lines.get(index);
        var end = index + 1 < lines.size()
            ? lines.get(index + 1)
            : source.length();
        return source.substring(start, end).stripTrailing();
    }

    /**
     * Get the line number, starting at 1, that contains this index.
     * @param index The index
     * @return The line number
     */
    public int index2line(int index) {
        for (var i = 0; i < lines.size(); i++) {
            var n = lines.get(i);
            System.out.println("index=" + index + " i=" + i + " n=" + n);
            if (index <= n) {
                return i + 1;
            }
        }
        return lines.size();
    }

    /**
     * Gets the line and column position of the index in the source.
     * Both lines and columns are numbered starting at 1.
     * @param index The index
     * @return The position record
     */
    public Position index2position(int index) {
        var line = index2line(index);
        var start = lines.get(line - 1);
        var column = (index - start) + 1;
        return new Position(line, column);
    }

    /**
     * Gets a span.
     * @param start The starting index
     * @param end The ending index (index to the character following the span)
     * @return The span
     */
    public Span span(int start, int end) {
        return new BufferSpan(start, end);
    }

    public Span synthetic(String text) {
        return new SyntheticSpan(text);
    }

    //-------------------------------------------------------------------------
    // Helper Types

    // A character position at a line and column.
    public record Position(int line, int column) {
        @Override
        public String toString() {
            return line + "," + column;
        }
    }

    /**
     * A span of text from a source file.
     */
    public interface Span {
        /**
         * The filename from which the span was drawn (or some other identifier).
         * @return the filename
         */
        String filename();

        /**
         * The text of the span.
         * @return The text.
         */
        String text();
    }

    private class SyntheticSpan implements Span {
        private final String text;

        public SyntheticSpan(String text) {
            this.text = text;
        }

        public String filename() {
            return filename;
        }

        public String text() {
            return text;
        }
    }

    private class BufferSpan implements Span {
        //---------------------------------------------------------------------
        // Instance variables

        private final int start;
        private final int end;

        //---------------------------------------------------------------------
        // Constructor

        public BufferSpan(int start, int end) {
            this.start = start;
            this.end = end;
        }

        //---------------------------------------------------------------------
        // Methods

        public String filename() {
            return filename;
        }

        public String text() {
            return source.substring(start, end);
        }
    }
}
