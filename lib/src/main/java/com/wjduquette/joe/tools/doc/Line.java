package com.wjduquette.joe.tools.doc;

public record Line(int number, String text) {
    public boolean isBlank() {
        return text.isBlank();
    }

    public boolean isTagged() {
        return text.trim().startsWith("@");
    }

    public Tag getTag() {
        var txt = text.trim();
        if (!txt.startsWith("@")) {
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
