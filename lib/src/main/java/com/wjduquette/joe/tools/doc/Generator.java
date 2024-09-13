package com.wjduquette.joe.tools.doc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class Generator {
    interface ContentFunction {
        void write(ContentWriter out);
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    private final DocConfig config;
    private final DocumentationSet docSet;

    //-------------------------------------------------------------------------
    // Constructor

    public Generator(DocConfig config, DocumentationSet docSet) {
        this.config = config;
        this.docSet = docSet;
    }

    //-------------------------------------------------------------------------
    // Public Methods

    /**
     * Generates the output for the documentation set given the
     * configuration.
     */
    public void generate() {
        // FIRST, generate the files for each package, in order.
        var packages = docSet.packages().stream()
            .sorted(Comparator.comparing(PackageEntry::name))
            .toList();
        for (var pkg : packages) {
            write(config.getOutputFolder().resolve(pkg.filename()),
                out -> writePackageFile(out, pkg));
        }
    }

    //-------------------------------------------------------------------------
    // Package Files

    private void writePackageFile(ContentWriter out, PackageEntry pkg) {
        // FIRST, output the header
        out.h1(pkg.h1Title());

        // NEXT, output the first paragraph of the content.
        var content = new ArrayList<>(pkg.content());
        contentIntro(content).forEach(out::println);

        // NEXT, output the package index.
        out.println();
        out.println("TODO: Package Index");
        out.println();

        // NEXT, output the remaining content
        content.forEach(out::println);

        // NEXT, output the entries for each of the package's functions.
        if (!pkg.functions().isEmpty()) {
            out.h2("Functions");
            writeCallables(out, pkg.functions());
        }
    }

    private void writeCallables(
        ContentWriter out,
        List<? extends Callable> callables
    ) {
        callables.forEach(c -> writeCallable(out, c));
        out.println();
    }

    private void writeCallable(ContentWriter out, Callable callable) {
        out.h3(callable.h3Title());
        out.hline();
        out.println("TODO: signature\n");
        callable.content().forEach(out::println);
        out.println();
    }

    //-------------------------------------------------------------------------
    // Entry Helpers

    // Extracts and returns the first paragraph of the content.
    private List<String> contentIntro(List<String> content) {
        var result = new ArrayList<String>();

        // FIRST, skip blank lines.
        while (!content.isEmpty() && content.getFirst().isBlank()) {
            content.removeFirst();
        }

        // NEXT, get lines until the end or the first blank.
        while (!content.isEmpty() && !content.getFirst().isBlank()) {
            result.add(content.removeFirst());
        }

        return result;
    }

    //-------------------------------------------------------------------------
    // File Output

    private void write(Path path, ContentFunction function) {
        System.out.println("Writing: " + path);
        try (var writer = Files.newBufferedWriter(path)) {
            var out = new ContentWriter(writer);
            function.write(out);
        } catch (IOException ex) {
            System.err.println("*** Failed to write " + path + ",\n   " +
                ex.getMessage());
        }
    }
}
