package com.wjduquette.joe.tools.doc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

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
        for (var pkg : sorted(docSet.packages(), PackageEntry::name)) {
            write(config.getOutputFolder().resolve(pkg.filename()),
                out -> writePackageFile(out, pkg));

            for (var type : sorted(pkg.types(), TypeEntry::name)) {
                write(config.getOutputFolder().resolve(type.filename()),
                    out -> writeTypeFile(out, type));
            }
        }
    }

    //-------------------------------------------------------------------------
    // Package Files

    private void writePackageFile(ContentWriter out, PackageEntry pkg) {
        // FIRST, output the header
        out.h1(h1PackageTitle(pkg));

        // NEXT, output the first paragraph of the content.
        var content = new ArrayList<>(pkg.content());
        contentIntro(content).forEach(out::println);

        // NEXT, output the package index.
        out.println();

        if (!pkg.functions().isEmpty()) {
            out.hb("functions", "Functions");
            out.println();
            pkg.functions().forEach(f -> writeCallableLink(out, 0, f));
            out.println();
        }

        if (!pkg.types().isEmpty()) {
            out.hb("Types");
            out.println();
            pkg.types().forEach(t -> writeTypeLink(out, 0, t));
            out.println();
        }

        // NEXT, output the remaining content
        content.forEach(out::println);

        // NEXT, output the entries for each of the package's functions.
        if (!pkg.functions().isEmpty()) {
            out.h2("functions", "Functions");
            writeCallableBodies(out, pkg.functions());
        }
    }

    private String h1PackageTitle(PackageEntry pkg) {
        return pkg.title() != null
            ? pkg.title() + " (" + mono(pkg.name()) + ")"
            : mono(pkg.name()) +  " package";
    }

    private void writeTypeLink(
        ContentWriter out,
        int indent,
        TypeEntry type
    ) {
        var leader = " ".repeat(indent);

        out.println(leader + "- [" + type.name() + " type](" +
            type.filename() + ")");
    }

    //-------------------------------------------------------------------------
    // Type Files

    private void writeTypeFile(ContentWriter out, TypeEntry type) {
        // FIRST, output the header
        out.h1(mono(type.name())
            + " type ("
            + mono(type.pkg().name())
            + ")");

        // NEXT, output the first paragraph of the content.
        var content = new ArrayList<>(type.content());
        contentIntro(content).forEach(out::println);

        // NEXT, output the type index.
        out.println();

        if (!type.constants().isEmpty()) {
            out.hb("constants", "Constants");
            out.println();
            type.constants()
                .forEach(c -> writeConstantLink(out, 0, c));
            out.println();
        }

        if (!type.staticMethods().isEmpty()) {
            out.hb("statics", "Static Methods");
            out.println();
            type.staticMethods()
                .forEach(m -> writeCallableLink(out, 0, m));
            out.println();
        }

        if (type.initializer() != null) {
            out.hb("init", "Initializer");
            out.println();
            writeCallableLink(out, 0, type.initializer());
            out.println();
        }

        if (!type.methods().isEmpty()) {
            out.hb("methods", "Methods");
            out.println();
            type.methods()
                .forEach(m -> writeCallableLink(out, 0, m));
            out.println();
        }

        // NEXT, output the remaining content
        content.forEach(out::println);

        // NEXT, output Constants.
        if (!type.constants().isEmpty()) {
            out.h2("constants", "Constants");
            writeConstantBodies(out, type.constants());
        }

        // NEXT, output Static Methods
        if (!type.staticMethods().isEmpty()) {
            out.h2("statics", "Static Methods");
            writeCallableBodies(out, type.staticMethods());
        }

        // NEXT, output Initializer
        if (type.initializer() != null) {
            out.h2("init", type.name() + " Initializer");
            writeInitializerBody(out, type.initializer());
        }

        // NEXT, output Instance Methods
        if (!type.methods().isEmpty()) {
            out.h2("methods","Methods");
            writeCallableBodies(out, type.methods());
        }
    }

    //-------------------------------------------------------------------------
    // Constants

    private void writeConstantBodies(
        ContentWriter out,
        List<ConstantEntry> constants
    ) {
        constants.forEach(c -> writeConstantBody(out, c));
        out.println();
    }

    private void writeConstantBody(ContentWriter out, ConstantEntry constant) {
        out.h3(constant.id(), constant.type().prefix() + "." + constant.name());
        constant.content().forEach(out::println);
        out.println();
    }

    private void writeConstantLink(
        ContentWriter out,
        int indent,
        ConstantEntry constant
    ) {
        var leader = " ".repeat(indent);
        out.println(leader + "- [" +
            constant.type().prefix() + "." + constant.name() +
            "](#" + constant.id() + ")"
        );
    }

    //-------------------------------------------------------------------------
    // Callables

    private void writeCallableBodies(
        ContentWriter out,
        List<? extends Callable> callables
    ) {
        callables.forEach(c -> writeCallableBody(out, c));
        out.println();
    }

    private void writeCallableLink(
        ContentWriter out,
        int indent,
        Callable callable
    ) {
        var leader = " ".repeat(indent);

        for (var sig : callableBodySignatures(callable)) {
            out.println(leader + "- [" + sig + "](#" + callable.id() + ")");
        }
    }

    private void writeCallableBody(ContentWriter out, Callable callable) {
        var title = switch(callable) {
            case StaticMethodEntry entry ->
                entry.prefix() + "." + entry.name() + "()";
            case MethodEntry entry ->
                ital(downCase(entry.prefix())) + "." + entry.name() + "()";
            default -> callable.name() + "()";
        };

        out.h3(callable.id(), title);
        out.println(String.join("<br>\n", callableBodySignatures(callable)));
        out.println();
        callable.content().forEach(out::println);
        out.println();
    }

    private void writeInitializerBody(ContentWriter out, InitializerEntry callable) {
        out.println(String.join("<br>\n", callableBodySignatures(callable)));
        out.println();
        callable.content().forEach(out::println);
        out.println();
    }

    private List<String> callableBodySignatures(Callable callable) {
        var result = new ArrayList<String>();

        var prefix = switch(callable) {
            case StaticMethodEntry entry -> entry.prefix() + ".";
            case MethodEntry entry -> ital(downCase(entry.prefix())) + ".";
            default -> "";
        };

        var name = prefix + callable.name();

        for (var spec : callable.argSpecs()) {
            StringBuilder buff = new StringBuilder();
            buff.append("**")
                .append(name)
                .append("(")
                .append(ArgSpec.asMarkdown(spec))
                .append(")");

            if (callable.result() != null) {
                buff.append(" → ").append(callable.result());
            }
            buff.append("**");
            result.add(buff.toString());
        }

        return result;
    }

    //-------------------------------------------------------------------------
    // Entry Helpers

    private String mono(String text) {
        return "`" + text + "`";
    }

    private String ital(String text) {
        return "*" + text + "*";
    }

    private <T> List<T> sorted(List<T> input, Function<T,String> getter) {
        return input.stream()
            .sorted(Comparator.comparing(getter))
            .toList();
    }

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

    private String downCase(String name) {
        if (!name.isEmpty()) {
            var ch = name.charAt(0);
            return Character.toLowerCase(ch) + name.substring(1);
        } else {
            return "";
        }
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
