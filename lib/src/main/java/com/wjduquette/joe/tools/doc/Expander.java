package com.wjduquette.joe.tools.doc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

// Expands source files into output markdown files for processing by
// mdBook.
class Expander {
    interface ContentFunction {
        void write(ContentWriter out);
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    // Constructor Arguments
    private final DocumentationSet docSet;
    private final boolean verbose;
    private final Path libFolder;


    //
    // Transients
    //

    // The current source file
    private transient Path sourceFile;

    // The lines to process
    private transient List<Line> lines = null;

    // The previous line after advance()
    private transient Line previous = null;

    // The current indent, in characters.
    private transient int indent = 0;

    //-------------------------------------------------------------------------
    // Constructor

    public Expander(DocConfig config, DocumentationSet docSet, boolean verbose) {
        this.docSet = docSet;
        this.verbose = verbose;

        // Compute libFolder, for index links
        // TODO: This is butt-ugly.
        var mdbookSrcFolder = Path.of("src").toAbsolutePath();
        var outputFolder = config.libraryFolder().toAbsolutePath();
        this.libFolder = mdbookSrcFolder.relativize(outputFolder);
    }

    //-------------------------------------------------------------------------
    // Public Methods

    public void expand(Path sourceFile, Path destFile) {
        this.sourceFile = sourceFile;

        if (verbose) {
            System.out.println("Transforming: " + sourceFile);
        }

        try {
            this.lines = readLines(sourceFile);
        } catch (IOException ex) {
            System.err.println("*** Failed to read " + sourceFile + ",\n   " +
                ex.getMessage());
            return;
        }

        this.indent = 0;
        write(destFile, this::process);
    }

    //-------------------------------------------------------------------------
    // Expansions

    private static final String INDENT = "@indent";
    private static final String PACKAGE = "@package";

    private void process(ContentWriter out) {
        while (!atEnd()) {
            // FIRST, advance to the next tag, writing intermediate output to
            // the file.
            if (!advanceToTag(out)) break;

            var tag = previous().getTag();

            switch (tag.name()) {
                case INDENT -> _indent(tag);
                case PACKAGE -> _package(out, tag);
                default -> throw error(previous(), "Unexpected tag: " + tag);
            }
        }
    }

    private void _indent(Tag tag) {
        try {
            var value = Integer.parseInt(tag.value());

            if (value >= 0) {
                this.indent = value;
                return;
            }
        } catch (Exception ex) {
            // Nothing to do
        }

        throw error(previous(),
            "Expected indent size in characters, got '" + tag.value() + "'.");
    }

    private void _package(ContentWriter out, Tag tag) {
        var pkgName = tag.value();
        var entry = docSet.lookup(pkgName);
        if (entry instanceof PackageEntry pkg) {
            var linkText = pkg.title() != null
                ? pkg.title() + " (" + pkg.name() + ")"
                : pkg.name();
            var url = libFolder.resolve(pkg.filename()).toString();

            out.print(indent() + "- ");
            out.link(linkText, url);
            out.println();

            for (var type : sorted(pkg.types(), TypeEntry::name)) {
                var typeText = type.name() + " " +
                    type.kind().name().toLowerCase();
                var typeUrl = libFolder.resolve(type.filename()).toString();
                out.print(indent(2) + "- ");
                out.link(typeText, typeUrl);
                out.println();
            }
        } else {
            throw error(previous(),
                "Expected package name, got '" + tag.value() + "'.");
        }
    }

    private String indent() {
        return indent(0);
    }

    private String indent(int extra) {
        return " ".repeat(indent + extra);
    }

    private <T> List<T> sorted(List<T> input, Function<T,String> getter) {
        return input.stream()
            .sorted(Comparator.comparing(getter))
            .toList();
    }

    // Advances to the next tag, or the end of the input, adding
    // content lines to the output.  Returns true if a tag is found,
    // and false otherwise.
    private boolean advanceToTag(ContentWriter out) {
        while (!atEnd() && !peek().isTagged()) {
            var line = advance();
            out.println(line.text());
        }

        if (!atEnd()) {
            advance();  // previous is now the tag line.
        }

        return !atEnd();
    }

    // Is there any input left?
    private boolean atEnd() {
        return lines.isEmpty();
    }

    // Peeks at the current line.
    private Line peek() {
        return lines.getFirst();
    }

    // Returns the previous line.
    private Line previous() {
        return previous;
    }

    // Advances to the next line, saving the current line as
    // previous().
    private Line advance() {
        previous = peek();
        return lines.removeFirst();
    }

    // ParseError is just a convenient way to break out of the processor.
    // We halt on the first error for now.
    static class ParseError extends RuntimeException { }

    private ParseError error(Line line, String message) {
        if (!verbose) {
            System.err.println("*** Error in " + sourceFile);
        }
        System.err.println("[line " + line.number() + "] " + message);
        System.err.println("  --> // " + line.text());
        return new ParseError();
    }

    //-------------------------------------------------------------------------
    // Helpers

    private List<Line> readLines(Path path) throws IOException {
        var strings = Files.readAllLines(path);
        var lines = new ArrayList<Line>();
        var count = 0;
        for (var line : strings) {
            lines.add(new Line(++count, line));
        }
        return lines;
    }

    private void write(Path path, ContentFunction function) {
        if (verbose) {
            System.out.println("  Writing: " + path);
        }

        try (var writer = Files.newBufferedWriter(path)) {
            var out = new ContentWriter(writer);
            function.write(out);
        } catch (IOException ex) {
            System.err.println("*** Failed to write " + path + ",\n   " +
                ex.getMessage());
        }
    }
}
