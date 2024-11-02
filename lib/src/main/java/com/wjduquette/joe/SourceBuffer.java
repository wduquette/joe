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
     * @param line The line number, starting at 1.
     * @return The text.
     */
    public String line(int line) {
        if (0 < line && line <= lines.size()) {
            var start = lines.get(line - 1);
            var end = endOfLine(line);
            return source.substring(start, end).stripTrailing();
        } else {
            throw new IllegalArgumentException("Line out of range.");
        }
    }

    /**
     * Get the line number, starting at 1, that contains this index.
     * @param index The index
     * @return The line number, or -1 if the index is out of range.
     */
    public int index2line(int index) {
        // FIRST, allow the character position just after the end
        // of the string as a valid index to the last line.
        if (index == source.length()) {
            return lines.size();
        }

        // NEXT, find the line that contains this index.
        for (var line = 1; line <= lines.size(); line++) {
            var start = lines.get(line - 1);
            var end = endOfLine(line);

            if (start <= index && index < end) {
                return line;
            }
        }
        return -1;
    }

    private int endOfLine(int line) {
        // NOTE: lines is indexed 0 to n-1, but line is an index
        // 1 to n.
        return line < lines.size()
            ? lines.get(line)  // Beginning of the next line
            : source.length(); // End of the string
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

    public Span lineSpan(int line) {
        if (0 < line && line <= lines.size()) {
            var start = lines.get(line - 1);
            var end = endOfLine(line);
            return span(start, end);
        } else {
            throw new IllegalArgumentException("Line out of range.");
        }
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
         * The source buffer to which this span belongs.
         * @return The buffer
         */
        SourceBuffer buffer();

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

        /**
         * The start index of this span.
         * @return The index
         */
        int start();

        /**
         * The end index of this span.
         * @return The index
         */
        int end();

        /**
         * Gets the number of the line containing the start of the span.
         * @return The line
         */
        default int startLine() {
            return buffer().index2line(start());
        }

        /**
         * Gets the number of the line containing the end of the span.
         * @return The line
         */
        default int endLine() {
            return buffer().index2line(end());
        }

        /**
         * Gets the position (line,column) of the start of the span
         * in the source.
         * @return The position
         */
        default Position startPosition() {
            return buffer().index2position(start());
        }

        /**
         * Gets the position (line,column) of the end of the span
         * in the source.
         * @return The position
         */
        default Position endPosition() {
            return buffer().index2position(end());
        }
    }

    private class SyntheticSpan implements Span {
        private final String text;

        public SyntheticSpan(String text) {
            this.text = text;
        }

        @Override public String filename() { return filename; }
        @Override public String text() { return text; }
        @Override public SourceBuffer buffer() { return SourceBuffer.this; }

        @Override
        public int start() {
            throw new UnsupportedOperationException("Synthetic span");
        }

        @Override
        public int end() {
            throw new UnsupportedOperationException("Synthetic span");
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


        @Override public SourceBuffer buffer() { return SourceBuffer.this; }
        @Override public String filename() { return filename; }
        @Override public String text() { return source.substring(start, end); }
        @Override public int start() { return start; }
        @Override public int end() { return end; }
    }
}
