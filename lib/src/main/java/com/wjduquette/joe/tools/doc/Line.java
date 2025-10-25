package com.wjduquette.joe.tools.doc;

/**
 * A line of doc comment text, stripped of its leading "//".
 * @param number The line number in the source file
 * @param text The text
 */
record Line(int number, String text) {
    /**
     * Returns true if the line is blank, and false otherwise.
     * @return true or false
     */
    public boolean isBlank() {
        return text.isBlank();
    }

    /**
     * Returns true if the line's non-whitespace text begins with
     * an "@" or "%" tag.
     * @return true or false
     */
    public boolean isTagged() {
        return text.trim().startsWith("@")
            || text.trim().startsWith("%");
    }

    /**
     * If isTagged(), returns the tag and its value.
     * @return The Tag
     */
    public Tag getTag() {
        var txt = text.trim();
        if (!isTagged()) {
            throw new IllegalStateException("Line has no tag.");
        }
        var ndx = txt.indexOf(" ");
        if (ndx == -1) {
            return new Tag(txt, "");
        } else {
            var tag = txt.substring(0, ndx);
            var value = txt.substring(ndx + 1);
            return new Tag(tag, value);
        }
    }

    @Override
    public String toString() {
        return String.format("[line %04d] %s", number, text);
    }
}
