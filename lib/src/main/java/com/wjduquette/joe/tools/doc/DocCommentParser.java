package com.wjduquette.joe.tools.doc;


import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class DocCommentParser {
    //-------------------------------------------------------------------------
    // Static Constants

    private static final boolean TRACE = false;

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
            // The error has already been output.
        }
    }

    //-------------------------------------------------------------------------
    // Parser

    private static final String PACKAGE = "@package";
    private static final String FUNCTION = "@function";
    private static final String TYPE = "@type";
    private static final String CONSTANT = "@constant";
    private static final String STATIC = "@static";
    private static final String INIT = "@init";
    private static final String METHOD = "@method";

    private static final Set<String> PACKAGE_ENDERS = Set.of(
        PACKAGE
    );

    private static final Set<String> PACKAGE_CHILD_ENDERS = Set.of(
        PACKAGE, FUNCTION, TYPE
    );

    private static final Set<String> TYPE_CHILD_ENDERS = Set.of(
        PACKAGE, FUNCTION, TYPE, CONSTANT, STATIC, INIT, METHOD
    );

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

    private void trace(Object... args) {
        if (TRACE) {
            var text = Arrays.stream(args)
                .map(Object::toString)
                .collect(Collectors.joining(" "));

            if (atEnd()) {
                System.out.println(text + "\n    peek: [At end]");
            } else {
                System.out.println(text + "\n    peek: " + peek());
            }
        }
    }

    private void _package(Tag pkgTag) {
        trace("_package", pkgTag);
        PackageEntry pkg = new PackageEntry(pkgTag.value());
        docSet.packages().add(pkg);

        while (!atEnd()) {
            if (!advanceToTag(pkg)) break;

            var tag = peek().getTag();

            if (PACKAGE_ENDERS.contains(tag.name())) {
                break;
            }

            advance();
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

        while (!atEnd()) {
            if (!advanceToTag(func)) break;

            var tag = peek().getTag();

            if (PACKAGE_CHILD_ENDERS.contains(tag.name())) {
                break;
            }

            advance();
            switch (tag.name()) {
                // TODO: Add metadata
                default -> throw error(previous(), "Unexpected tag: " + tag);
            }
        }
    }

    private void _type(PackageEntry pkg, Tag typeTag) {
        TypeEntry type = new TypeEntry(pkg, typeTag.value());
        pkg.types().add(type);

        while (!atEnd()) {
            if (!advanceToTag(type)) break;

            var tag = peek().getTag();

            if (PACKAGE_CHILD_ENDERS.contains(tag.name())) {
                break;
            }

            advance();
            switch (tag.name()) {
                case CONSTANT -> _constant(type, tag);
                case STATIC -> _static(type, tag);
                case INIT -> _init(type, tag);
                case METHOD -> _method(type, tag);
                default -> throw error(previous(), "Unexpected tag: " + tag);
            }
        }
    }

    private void _constant(TypeEntry type, Tag constantTag) {
        ConstantEntry constant = new ConstantEntry(type, constantTag.value());
        type.constants().add(constant);

        while (!atEnd()) {
            if (!advanceToTag(constant)) break;

            var tag = peek().getTag();

            if (TYPE_CHILD_ENDERS.contains(tag.name())) {
                break;
            }

            advance();
            switch (tag.name()) {
                default -> throw error(previous(), "Unexpected tag: " + tag);
            }
        }
    }

    private void _static(TypeEntry type, Tag methodTag) {
        StaticMethodEntry method = new StaticMethodEntry(type, methodTag.value());
        type.staticMethods().add(method);

        while (!atEnd()) {
            if (!advanceToTag(method)) break;

            var tag = peek().getTag();

            if (TYPE_CHILD_ENDERS.contains(tag.name())) {
                break;
            }

            advance();
            switch (tag.name()) {
                default -> throw error(previous(), "Unexpected tag: " + tag);
            }
        }
    }

    private void _init(TypeEntry type, Tag initTag) {
        InitializerEntry init = new InitializerEntry(type, initTag.value());
        type.setInitializer(init);

        while (!atEnd()) {
            if (!advanceToTag(init)) break;

            var tag = peek().getTag();

            if (TYPE_CHILD_ENDERS.contains(tag.name())) {
                break;
            }

            advance();
            switch (tag.name()) {
                default -> throw error(previous(), "Unexpected tag: " + tag);
            }
        }
    }

    private void _method(TypeEntry type, Tag methodTag) {
        MethodEntry method = new MethodEntry(type, methodTag.value());
        type.methods().add(method);

        while (!atEnd()) {
            if (!advanceToTag(method)) break;

            var tag = peek().getTag();

            if (TYPE_CHILD_ENDERS.contains(tag.name())) {
                break;
            }

            advance();
            switch (tag.name()) {
                default -> throw error(previous(), "Unexpected tag: " + tag);
            }
        }
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
                entry.content().add(line.text());
                trace("advanceToTag", previous());
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
