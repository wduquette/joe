package com.wjduquette.joe.tools.doc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

// Expands `docInputFolder` files into `docOutputFolder` files
// mdBook.

/**
 * Copies the {@code docInputFolder} tree to the {@code docOutputFolder},
 * expanding `@`-tags and `[[...]]` links in all copied `.md` files.
 */
class Processor {
    interface ContentFunction {
        void write(ContentWriter out);
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    // Constructor Arguments
    private final DocConfig config;
    private final DocumentationSet docSet;
    private final boolean verbose;

    //
    // Transients
    //

    // The current input and output files
    private transient Path currentInput;

    // The lines to process
    private transient List<Line> lines = null;

    // The previous line after advance()
    private transient Line previous = null;

    // The current indent, in characters.
    private transient int indent = 0;

    //-------------------------------------------------------------------------
    // Constructor

    public Processor(DocConfig config, DocumentationSet docSet, boolean verbose) {
        this.config = config;
        this.docSet = docSet;
        this.verbose = verbose;
    }

    //-------------------------------------------------------------------------
    // Public Methods

    public void process() {
        if (config.docInputFolder() == null) return;
        if (config.docOutputFolder() == null) {
            System.err.println(
                "*** Cannot process input files: the docOutputFolder is undefined.");
            throw new ProcessError();
        }

        prepareDocOutputFolder();
        for (var inputFile : getInputFiles()) {
            try {
                if (inputFile.toString().endsWith(".md")) {
                    expandMarkdownFile(inputFile);
                } else {
                    copyFile(inputFile);
                }
            } catch (Exception ex) {
                System.err.println("*** Failed to process " + inputFile +
                    ": " + ex.getMessage());
                throw new ProcessError();
            }
        }
    }

    private void prepareDocOutputFolder() {
        try {
            if (!Files.exists(config.docOutputFolder())) {
                Files.createDirectories(config.docOutputFolder());
            } else {
                // Get the paths in reverse order by length, so that we delete
                // files before their folders.
                var paths = getFilesIn(config.docOutputFolder()).stream()
                    .sorted(Comparator.comparing(Path::getNameCount))
                    .toList()
                    .reversed();
                for (var path : paths) {
                    if (path.equals(config.docOutputFolder())) continue;
                    Files.delete(path);
                }
            }
        } catch (Exception ex) {
            System.err.println(
                "*** Could not prepare docOutputFolder for processed output: " +
                    ex.getMessage());
            throw new ProcessError();
        }
    }

    private List<Path> getInputFiles() {
        try {
            return getFilesIn(config.docInputFolder());
        } catch (IOException ex) {
            System.err.println(
                "*** Could not read input files from docInputFolder: " +
                    ex.getMessage());
            throw new ProcessError();
        }
    }

    // Gets the files in the given folder tree.  Returns resolved paths.
    private List<Path> getFilesIn(Path folder) throws IOException {
        try (Stream<Path> stream = Files.walk(folder, Integer.MAX_VALUE)) {
            return stream
                .filter(Files::isRegularFile)
                .toList();
        }
    }

    private void copyFile(Path inputFile) throws Exception {
        var relFile = config.docInputFolder().relativize(inputFile);
        var outputFile = config.docOutputFolder().resolve(relFile);
        Files.createDirectories(outputFile.getParent());
        if (verbose) {
            System.out.println("Copying: " + inputFile);
        }
        Files.copy(inputFile, outputFile);
    }

    private void expandMarkdownFile(Path inputFile) throws Exception {
        var relFile = config.docInputFolder().relativize(inputFile);
        var outputFile = config.docOutputFolder().resolve(relFile);
        Files.createDirectories(outputFile.getParent());
        if (verbose) {
            System.out.println("Expanding: " + inputFile);
        }

        try {
            currentInput = inputFile;
            this.lines = readLines(inputFile);
        } catch (IOException ex) {
            System.err.println("*** Failed to read " + inputFile + ",\n   " +
                ex.getMessage());
            return;
        }

        this.indent = 0;
        write(outputFile, this::process);
    }

    //-------------------------------------------------------------------------
    // Expansions

    private static final String INDENT = "@:indent";
    private static final String PACKAGE_INDEX = "@:packageIndex";

    private void process(ContentWriter out) {
        while (!atEnd()) {
            // FIRST, advance to the next tag, writing intermediate lines to
            // the file.
            if (!advanceToTag(out)) break;

            var tag = previous().getTag();

            switch (tag.name()) {
                case INDENT -> _indent(tag);
                case PACKAGE_INDEX -> _packageIndex(out, tag);
                default -> warn(previous(), "Unexpected tag: " + tag.name());
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

    private void _packageIndex(ContentWriter out, Tag tag) {
        var pkgName = tag.value();
        var entry = docSet.lookup(pkgName);
        if (entry instanceof PackageEntry pkg) {
            var linkText = pkg.title() != null
                ? pkg.title() + " (" + pkg.name() + ")"
                : pkg.name();
            var url = config.libOutputFolder().resolve(pkg.filename()).toString();

            out.print(indent() + "- ");
            out.link(linkText, url);
            out.println();

            for (var type : sorted(pkg.types(), TypeEntry::name)) {
                var typeText = type.name() + " " +
                    type.kind().name().toLowerCase();
                var typeUrl = config.libOutputFolder()
                    .resolve(type.filename()).toString();
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

    // ProcessError is just a convenient way to break out of the processor.
    // We halt on the first error for now.
    static class ProcessError extends RuntimeException { }

    private void warn(Line line, String message) {
        System.out.println("*** Warning in: " + currentInput);
        System.err.println("  [line " + line.number() + "] " + message);
        System.err.println("  --> " + line.text());
    }

    private ProcessError error(Line line, String message) {
        System.err.println("*** Error in " + currentInput);
        System.err.println("  [line " + line.number() + "] " + message);
        System.err.println("  --> " + line.text());
        return new ProcessError();
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

    //-------------------------------------------------------------------------
    // Helper Classes

    /**
     * A line of doc file text.
     * @param number The line number in the source file
     * @param text The text
     */
    record Line(int number, String text) {
        /**
         * Returns true if the line's text begins with
         * an "@" or "%" tag in the first column.
         *
         * @return true or false
         */
        public boolean isTagged() {
            return text.startsWith("@:") || text.startsWith("%:");
        }

        /**
         * If isTagged(), returns the tag and its value.
         *
         * @return The Tag
         */
        public Tag getTag() {
            var txt = text.stripTrailing();
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

}
