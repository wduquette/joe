package com.wjduquette.joe.tools.doc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

class Generator {
    public static final String DOC_SET_INDEX = "index.md";

    interface ContentFunction {
        void write(ContentWriter out);
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    // Constructor Arguments
    private final DocConfig config;
    private final DocumentationSet docSet;
    private final boolean verbose;

    // Transient
    private final transient Map<String,Entry> shortTable = new HashMap<>();
    private Path outFile = null;

    //-------------------------------------------------------------------------
    // Constructor

    public Generator(DocConfig config, DocumentationSet docSet, boolean verbose) {
        this.config = config;
        this.docSet = docSet;
        this.verbose = verbose;
    }

    //-------------------------------------------------------------------------
    // Public Methods

    /**
     * Generates the output for the documentation set given the
     * configuration.
     */
    public void generate() {
        // FIRST, create the output folder (if it doesn't already exist)
        try {
            Files.createDirectories(config.outputFolder());
        } catch (Exception ex) {
            System.out.println("*** Could not create output folder:\n" +
                "  " + ex.getMessage());
        }

        // NEXT, prepare for generation
        for (var pkg : docSet.packages()) {
            // FIRST, populate the short mnemonic lookup table for this
            // package
            populateShortTable(pkg);

            // NEXT, include any mixins.
            for (var type : pkg.types()) {
                if (!type.mixins().isEmpty()) {
                    includeMixins(type);
                }
            }
        }

        // NEXT, generate the index file.
        write(config.outputFolder().resolve(DOC_SET_INDEX),
            this::writeDocSetIndex);

        // NEXT, generate the files for each package, in order.
        for (var pkg : sorted(docSet.packages(), PackageEntry::name)) {
            // FIRST, populate the short mnemonic lookup table for this
            // package
            populateShortTable(pkg);

            // NEXT, write the package file
            write(config.outputFolder().resolve(pkg.filename()),
                out -> writePackageFile(out, pkg));

            // NEXT, write each type file
            for (var type : sorted(pkg.types(), TypeEntry::name)) {
                write(config.outputFolder().resolve(type.filename()),
                    out -> writeTypeFile(out, type));
            }
        }
    }

    private void includeMixins(TypeEntry type) {
        for (var mixinName : type.mixins()) {
            var mixin = docSet.mixins().get(mixinName);
            if (mixin == null) {
                warn("Unknown mixin '" + mixinName + "' in type " + type);
                continue;
            }

            type.includeMixin(mixin);
        }
    }

    private void populateShortTable(PackageEntry pkg) {
        shortTable.clear();
        pkg.entries().forEach(e -> shortTable.put(e.shortMnemonic(), e));
    }

    //-------------------------------------------------------------------------
    // Documentation Set Index

    private void writeDocSetIndex(ContentWriter out) {
        out.h1line("Library API Index");
        out.println();
        out.println("""
            The following is a complete index of the packages, functions,
            types, methods, and constants include in Joe's aPackage.
            """);
        out.println();

        for (var pkg : sorted(docSet.packages(), PackageEntry::name)) {
            populateShortTable(pkg);

            out.println("- [" + packageIndexTitle(pkg) + "](" +
                pkg.filename() + ")");

            for (var fn : sorted(pkg.functions(), Callable::name)) {
                writeCallableLink(out, 2, fn);
            }

            for (var type : sorted(pkg.types(), TypeEntry::name)) {
                writeTypeLink(out, 2, type);

                sorted(type.constants(), ConstantEntry::name)
                    .forEach(c -> writeConstantLink(out, 4, c));

                sorted(type.staticMethods(), StaticMethodEntry::name)
                    .forEach(m -> writeCallableLink(out, 4, m));

                if (type.initializer() != null) {
                    writeCallableLink(out, 4, type.initializer());
                }

                sorted(type.methods(), MethodEntry::name)
                    .forEach(m -> writeCallableLink(out, 4, m));
            }
        }
    }

    //-------------------------------------------------------------------------
    // Package Files

    private void writePackageFile(ContentWriter out, PackageEntry pkg) {
        // FIRST, output the header
        out.h1line(packageH1Title(pkg));

        // NEXT, output the first paragraph of the content.
        var content = expandMnemonicLinks(pkg.content());
        contentIntro(content).forEach(out::println);

        // NEXT, output the package index.
        out.println();

        if (!pkg.topics().isEmpty()) {
            out.hblink("topics", "Topics");
            out.println();
            pkg.topics().forEach(t -> writeTopicLink(out, t));
            out.println();
        }

        if (!pkg.functions().isEmpty()) {
            out.hblink("functions", "Functions");
            out.println();
            sorted(pkg.functions(), Callable::name)
                .forEach(f -> writeCallableLink(out, 0, f));
            out.println();
        }

        var types = pkg.types();

        if (!types.isEmpty()) {
            out.hb("Types");
            out.println();
            sorted(types, TypeEntry::name)
                .forEach(t -> writeTypeLink(out, 0, t));
            out.println();
        }

        // NEXT, output the remaining content
        content.forEach(out::println);

        // NEXT, output the topics
        for (var topic : pkg.topics()) {
            writeTopicBody(out, topic);
        }

        // NEXT, output the entries for each of the package's functions.
        if (!pkg.functions().isEmpty()) {
            out.h2("functions", "Functions");
            writeCallableBodies(out, pkg.functions());
        }
    }

    private String packageH1Title(PackageEntry pkg) {
        return pkg.title() != null
            ? pkg.title() + " (" + mono(pkg.name()) + ")"
            : mono(pkg.name()) +  " package";
    }

    private String packageIndexTitle(PackageEntry pkg) {
        return pkg.title() != null
            ? mono(pkg.name()) +  " package (" + pkg.title() + ")"
            : mono(pkg.name()) + " package";
    }

    private void writeTypeLink(
        ContentWriter out,
        int indent,
        TypeEntry type
    ) {
        var leader = " ".repeat(indent);

        out.print(leader + "- [" + type.name() + "](" +
            type.filename() + ") " + type.kind().name().toLowerCase());
        out.println();
    }


    //-------------------------------------------------------------------------
    // Type Files

    private void writeTypeFile(ContentWriter out, TypeEntry type) {
        // FIRST, output the header
        out.h1(type.name()
            + " "
            + type.kind().name().toLowerCase()
            + " ("
            + mono(type.pkg().name())
            + ")");

        writeTypeHierarchy(out, type);
        out.println();
        out.hline();

        // NEXT, output the first paragraph of the content.
        var content = expandMnemonicLinks(type.content());
        contentIntro(content).forEach(out::println);

        // NEXT, output the type index.
        out.println();

        if (!type.topics().isEmpty()) {
            out.hblink("topics", "Topics");
            out.println();
            type.topics().forEach(t -> writeTopicLink(out, t));
            out.println();
        }

        if (!type.constants().isEmpty()) {
            out.hb("constants", "Constants");
            out.println();
            out.println("| Constant | Type | Description |");
            out.println("|----------|------|-------------|");
            for (var c : type.constants()) {
                out.printf("| `%s` | %s | %s |\n",
                    c.name(),
                    c.valueType() != null
                        ? typeLinkOrName(c.valueType())
                        : "-",
                    flatLine(c.content())
                );
            }
            out.println();
        }

        if (!type.staticMethods().isEmpty()) {
            out.hblink("statics", "Static Methods");
            out.println();
            sorted(type.staticMethods(), StaticMethodEntry::name)
                .forEach(m -> writeCallableLink(out, 0, m));
            out.println();
        }

        if (type.initializer() != null) {
            out.hblink("init", "Initializer");
            out.println();
            writeCallableLink(out, 0, type.initializer());
            out.println();
        }

        if (!type.fields().isEmpty()) {
            out.hb("fields", "Fields");
            out.println();
            out.println("| Field | Type | Description |");
            out.println("|-------|------|-------------|");
            for (var f : type.fields()) {
                out.printf("| `%s` | %s | %s |\n",
                    f.name(),
                    f.valueType() != null
                        ? typeLinkOrName(f.valueType())
                        : "-",
                    flatLine(f.content())
                );
            }
            out.println();
        }

        if (!type.properties().isEmpty()) {
            out.hb("properties", "JavaFX Properties");
            out.println();
            out.println("| Defined By | Property | Type | Description |");
            out.println("|------------|----------|------|-------------|");

            var supertype = type;
            while (supertype != null) {
                for (var p : supertype.properties()) {
                    out.printf("| %s | `#%s` | %s | %s |\n",
                        typeLinkOrName(supertype.name()),
                        p.name(),
                        p.valueType() != null
                            ? typeLinkOrName(p.valueType())
                            : "-",
                        flatLine(p.content())
                    );
                }
                supertype = lookupType(supertype.supertypeName());
            }
            out.println();
        }

        if (!type.methods().isEmpty()) {
            out.hblink("methods", "Methods");
            out.println();
            sorted(type.methods(), MethodEntry::name)
                .forEach(m -> writeCallableLink(out, 0, m));
        }

        var supertype = lookupType(type.supertypeName());
        while (supertype != null) {
            out.println("- " + typeLinkOrName(supertype.name()) + " methods");
            sorted(supertype.methods(), MethodEntry::name)
                .forEach(m -> writeCallableLink(out, 2, m));
            supertype = lookupType(supertype.supertypeName());
        }

        // NEXT, output the remaining content
        content.forEach(out::println);

        // NEXT, output the topics
        for (var topic : type.topics()) {
            writeTopicBody(out, topic);
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

    private void writeTypeHierarchy(ContentWriter out, TypeEntry type) {
        var supertypes = supertypeLinks(type);
        var subtypes = subtypeLinks(type);

        if (supertypes.isEmpty() && subtypes.isEmpty()) return;

        out.print("**Hierarchy**: ");
        if (!supertypes.isEmpty()) {
            out.print(supertypes + " ← ");
        }
        out.print("**" + type.name() + "**");
        if (!subtypes.isEmpty()) {
            out.print(" ← {" + subtypes + "}");
        }
        out.println();
    }

    private String supertypeLinks(TypeEntry type) {
        var links = new ArrayList<String>();
        var supertype = lookupType(type.supertypeName());
        while (supertype != null) {
            links.add(typeLinkOrName(supertype.name()));
            supertype = lookupType(supertype.supertypeName());
        }
        return String.join(" ← ", links.reversed());
    }

    private String subtypeLinks(TypeEntry type) {
        List<String> subtypes = docSet.entries().stream()
            .filter(e -> e instanceof TypeEntry)
            .map(e -> (TypeEntry)e)
            .filter(t -> type.shortMnemonic().equals(t.supertypeName()) ||
                type.fullMnemonic().equals(t.supertypeName()))
            .map(t -> typeLinkOrName(t.shortMnemonic()))
            .toList();
        return String.join(", ", subtypes);
    }

    //-------------------------------------------------------------------------
    // Topics

    private void writeTopicBody(ContentWriter out, TopicEntry topic) {
        out.h2(topic.id(), topic.title());
        expandMnemonicLinks(topic.content()).forEach(out::println);
        out.println();
    }

    private void writeTopicLink(
        ContentWriter out,
        TopicEntry topic
    ) {
        out.println("- [" +
            topic.title() +
            "](" + topic.filename() + "#" + topic.id() + ")"
        );
    }

    //-------------------------------------------------------------------------
    // Constants

    @SuppressWarnings("SameParameterValue")
    private void writeConstantLink(
        ContentWriter out,
        int indent,
        ConstantEntry constant
    ) {
        var leader = " ".repeat(indent);
        out.println(leader + "- [" +
            constant.type().prefix() + "." + constant.name() +
            "](" +constant.filename() + "#constants)"
        );
    }

    //-------------------------------------------------------------------------
    // Callables

    private void writeCallableBodies(
        ContentWriter out,
        List<? extends Callable> callables
    ) {
        sorted(callables, Callable::name)
            .forEach(c -> writeCallableBody(out, c));
        out.println();
    }

    private void writeCallableLink(
        ContentWriter out,
        int indent,
        Callable callable
    ) {
        var leader = " ".repeat(indent);

        for (var sig : signatures(callable)) {
            out.print(leader
                + "- "
                + link(sig, callable.filename() + "#" + callable.id())
            );

            if (callable.result() != null) {
                out.print(" → " + typeLinkOrName(callable.result()));
            }
            out.println();
        }
    }

    private void writeCallableBody(ContentWriter out, Callable callable) {
        var title = switch(callable) {
            case StaticMethodEntry entry ->
                entry.prefix() + "." + entry.name() + "()";
            case MethodEntry entry ->
                ital(entry.prefix()) + "." + entry.name() + "()";
            default -> callable.name() + "()";
        };

        out.h3(callable.id(), title);
        out.println(plainSignatures(callable));
        out.println();
        expandMnemonicLinks(callable.content()).forEach(out::println);
        out.println();
    }

    private void writeInitializerBody(ContentWriter out, InitializerEntry callable) {
        out.println(plainSignatures(callable));
        out.println();
        expandMnemonicLinks(callable.content()).forEach(out::println);
        out.println();
    }

    private String plainSignatures(Callable callable) {
        var result = new ArrayList<String>();

        for (var sig : signatures(callable)) {
            result.add(callable.result() != null
                ? "**" + sig + " → " + typeLinkOrName(callable.result()) + "**"
                : "**" + sig + "**");
        }
        return String.join("<br>\n", result);
    }

    private List<String> signatures(Callable callable) {
        var signatures = new ArrayList<String>();

        var prefix = switch(callable) {
            case StaticMethodEntry entry -> entry.prefix() + ".";
            case MethodEntry entry -> ital(entry.prefix()) + ".";
            default -> "";
        };

        var name = prefix + callable.name();

        var argSpecs = !callable.argSpecs().isEmpty()
            ? callable.argSpecs()
            : List.of("");

        for (var spec : argSpecs) {
            var sig = name + "(" + ArgSpec.asMarkdown(spec) + ")";
            signatures.add(sig);
        }

        return signatures;
    }

    private String typeLinkOrName(String name) {
        var type = lookupType(name);

        if (type != null) {
            return "[" + type.name() + "](" + type.filename() + ")";
        } else {
            return name;
        }
    }

    private TypeEntry lookupType(String name) {
        if (name == null) {
            return null;
        }

        var entry = docSet.lookup(name);

        if (entry == null) {
            entry = shortTable.get(name);
        }

        if (entry instanceof TypeEntry type) {
            return type;
        } else {
            return null;
        }
    }

    //-------------------------------------------------------------------------
    // Mnemonic Links

    private List<String> expandMnemonicLinks(List<String> content) {
        var list = new ArrayList<String>();
        content.stream()
            .map(this::expandLinks)
            .forEach(list::add);
        return list;
    }

    private String expandLinks(String line) {
        var buff = new StringBuilder();
        var head = line;
        int ndx;

        while ((ndx = head.indexOf("[[")) != -1) {
//            buff.append(head.substring(0, ndx));
            buff.append(head, 0, ndx);
            head = head.substring(ndx);

            // Check for incomplete link
            var tail = head.indexOf("]]");
            if (tail == -1) {
                buff.append(head);
                return buff.toString();
            }

            // Next, extract the entire mnemonic.
            var linkSpec = head.substring(2, tail);
            head = head.substring(tail + 2);

            // Next, look for link text.
            var tokens = linkSpec.split("\\|");
            var mnemonic = tokens[0];

            // NEXT, get the entity.  If not found, issue a warning and
            // leave a placeholder.
            var entry = lookupMnemonic(mnemonic);
            if (entry == null) {
                // Leave it in place; it's incorrect.
                warn("Unknown mnemonic in link: [[" + mnemonic + "]]");
                buff.append("[[").append(linkSpec).append("]]");
            } else {
                var linkText = tokens.length > 1
                    ? tokens[1]
                    : inlineLinkText(entry);
                buff.append(link(linkText, entry.url()));
            }
        }

        buff.append(head);
        return buff.toString();
    }

    private Entry lookupMnemonic(String mnemonic) {
        var entry = docSet.lookup(mnemonic);
        if (entry == null) entry = shortTable.get(mnemonic);
        return entry;
    }

    private String inlineLinkText(Entry entry) {
        return switch (entry) {
            case PackageEntry pkg -> mono(pkg.name());
            case FunctionEntry fn -> mono(fn.name() + "()");
            case TypeEntry t -> mono(t.name());
            case ConstantEntry c
                -> mono(c.type().name() + "." + c.name());
            case StaticMethodEntry m
                -> mono(m.type().name() + "." + m.name() + "()");
            case InitializerEntry fn
                -> mono(fn.name() + "()");
            case FieldEntry f
                -> mono(f.prefix() + "." + f.name());
            case PropertyEntry p
                -> mono("#" + p.name());
            case MethodEntry m
                -> mono(m.name() + "()");
            case TopicEntry t
                -> t.title();
            default -> throw new IllegalArgumentException("Unknown entry type!");
        };
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

    // Extracts and returns the content as a single line.
    private String flatLine(List<String> content) {
        return String.join(" ", expandMnemonicLinks(content))
            .replaceAll("\\s+", " ");
    }

    private String link(String text, String url) {
        return "[" + text + "](" + url + ")";
    }

    private void warn(String message) {
        if (!verbose) {
            if (outFile != null) {
                System.out.println("In file: " + outFile);
            }
            outFile = null;
        }
        System.out.println("  *** " + message);
    }

    //-------------------------------------------------------------------------
    // File Output

    private void write(Path path, ContentFunction function) {
        outFile = path;

        if (verbose) {
            System.out.println("Writing: " + path);
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
