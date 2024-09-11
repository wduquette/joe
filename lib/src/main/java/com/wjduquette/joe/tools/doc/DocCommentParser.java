package com.wjduquette.joe.tools.doc;

import javafx.css.CssParser;

import java.nio.file.Path;
import java.util.List;

class DocCommentParser {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final DocumentationSet docSet;
    private transient List<Line> lines;
    private transient Line previous = null;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * This parser will parse file content into the documentation set.
     * @param docSet The documentation set
     */
    DocCommentParser(DocumentationSet docSet) {
        this.docSet = docSet;
    }

    //-------------------------------------------------------------------------
    // API

    void parse(Path docFile) {
        // FIRST, extract the lines from the file.
        lines = Extractor.process(docFile);
        if (lines.isEmpty()) {
            return;
        }

        // NEXT, begin parsing.  There might be lines before the first tag;
        // if any are not blank, that's an error.
        System.out.println("Reading: " + docFile);

        try {
            parse();
        } catch (ParseError ex) {
            return;
        }
    }

    //-------------------------------------------------------------------------
    // Parser

    private static final String PACKAGE = "@package";
    private static final String FUNCTION = "@function";
    private static final String TYPE = "@type";

    private void parse() {
        while(!atEnd()) {
            if (!advanceToTag(null)) return;

            var tag = advance().getTag();

            switch (tag.name()) {
                case PACKAGE -> _package(tag);
                default -> throw error(previous(), "Unexpected tag: " + tag);
            }
        }
    }

    private void _package(Tag pkgTag) {
        PackageEntry pkg = new PackageEntry(pkgTag.value());
        docSet.packages().add(pkg);

        while (!atEnd()) {
            if (!advanceToTag(pkg)) break;

            var tag = advance().getTag();

            switch (tag.name()) {
                case FUNCTION -> _function(pkg, tag);
                case TYPE -> _type(pkg, tag);
                default -> throw error(previous(), "Unexpected tag: " + tag);
            }
        }
    }

    private void _function(PackageEntry pkg, Tag funcTag) {
        FunctionEntry func = new FunctionEntry(pkg, funcTag.value());
        pkg.functions().add(func);
        // TODO
    }

    private void _type(PackageEntry pkg, Tag typeTag) {
        TypeEntry type = new TypeEntry(pkg, typeTag.value());
        pkg.types().add(type);
        // TODO
    }

    //-------------------------------------------------------------------------
    // Primitives

    // ParseError is just a convenient way to break out of the parser.
    // We halt on the first error for now.
    private static class ParseError extends RuntimeException { }

    // Is there any input left?
    private boolean atEnd() {
        return lines.isEmpty();
    }

    // Peeks at the current line.
    private Line peek() {
        return lines.getFirst();
    }

    // Advances to the next line, saving the current line as
    // previous().
    private Line advance() {
        previous = peek();
        return lines.removeFirst();
    }

    // Advances to the next tag, or the end of the input, adding
    // content lines to the entry.  Returns true if a tag is found,
    // and false otherwise.
    private boolean advanceToTag(Entry entry) {
        while (!atEnd() && !peek().isTagged()) {
            var line = advance();
            if (entry == null) {
                if (!line.isBlank()) {
                    throw error(line, "Unexpected comment text before first entry tag.");
                }
            } else {
                entry.content().add(advance().text());
            }
        }

        return !atEnd();
    }

    // Returns the previous line.
    private Line previous() {
        return previous;
    }

    private ParseError error(Line line, String message) {
        System.err.println("[line " + line.number() + "] " + message);
        System.err.println("  --> // " + line.text());
        return new ParseError();
    }

}
